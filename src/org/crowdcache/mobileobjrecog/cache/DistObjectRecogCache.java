package org.crowdcache.mobileobjrecog.cache;

import org.apache.commons.lang3.ArrayUtils;
import org.crowdcache.Cache;
import org.crowdcache.mobileobjrecog.KeypointDescList;
import org.crowdcache.rpc.CrowdRPC;
import org.crowdcache.rpc.GetRequestCallback;
import org.crowdcache.rpc.GetResponseCallback;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Created by utsav on 2/19/16.
 */
public class DistObjectRecogCache extends ObjectRecogCache
{
    private final CrowdRPC rpc;
    private final ByteBuffer buffer = ByteBuffer.allocate(80 * 1024);
    Long start;
    public DistObjectRecogCache(Integer size)
    {
        super(size);
        this.rpc = new CrowdRPC(new RequestCallback());
        rpc.start();
    }

    /**
     * Check across all caches for image at given path
     * @param imgpath
     * @return
     */
    @Override
    public Result<String> get(String imgpath)
    {
        start = System.currentTimeMillis();
//        buffer.clear();
//        try
//        {
//            File newpath = reduce(imgpath);
//            FileInputStream fis = new FileInputStream(newpath.getAbsolutePath());
//            FileChannel fc = fis.getChannel();
//            fc.read(buffer);
//            buffer.flip();
//            fc.close();
//            newpath.delete();
//            byte[] data = buffer.array();
            return this.get(reduce(imgpath));
//        }
//        catch (IOException e)
//        {
//            e.printStackTrace();
//        }
//        return null;
    }

    @Override
    public Result<String> get(Mat mat)
    {
        KeypointDescList list = extractor.extract(mat);
        int rows = list.descriptions.rows();
        int cols = list.descriptions.cols();
        int type = list.descriptions.type();
        byte[] data = new byte[(int) (mat.total()*mat.elemSize())];
        ByteBuffer buf = ByteBuffer.allocate((int) (mat.total()*mat.elemSize()) + Integer.SIZE*3);
        buf.putInt(rows);
        buf.putInt(cols);
        buf.putInt(type);
        list.descriptions.get(0, 0, data);
        buf.put(data);
        byte[] senddata;
        try
        {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DeflaterOutputStream out = new DeflaterOutputStream(bos);
            out.write(buf.array());
            out.flush();
            out.close();
            return this.get(bos.toByteArray());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return new Result<>("None");
    }

    @Override
    public Result<String> get(byte[] imgpath)
    {
        // Create a new callback
        RespCallback callback = new RespCallback();

        // Issue rpc request
        this.rpc.get(imgpath, callback);

        // Compute on local data
//        Result<String> localres = super.get(imgpath);
        Result<String> localres = new Result<>(Double.MAX_VALUE, "None");

        try
        {
            Thread.sleep(400);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        // Get the remote request's results
        Result<String> remoteres = callback.get();
        //TODO This might be null. Can add a timeout for more results to arrive
        if(remoteres != null)
            if(remoteres.confidence < localres.confidence)
                return remoteres;

        return localres;
    }

    public class RespCallback extends GetResponseCallback
    {
        Double max = Double.MAX_VALUE;
        Byte[] res = null;

        @Override
        public void done(Result<Byte[]> result)
        {
            Long end = System.currentTimeMillis() - start;
            System.out.println("Got response " + new String(ArrayUtils.toPrimitive(result.value)) + ":" + result.confidence + " from peer in " + end);
            if(result.confidence < max)
            {
                max = result.confidence;
                res = result.value;
            }
        }

        /**
         * Get the best result found till now
         * @return
         */
        protected Result<String> get()
        {
            if(res != null)
                return new Result<>(this.max, new String(ArrayUtils.toPrimitive(this.res)));
            return null;
        }
    }

    /**
     * Wrapper around {@link ObjectRecogCache#get(byte[])} for remote get requests
     * @param compdata
     * @return
     */
    protected Result<String> remoteget(byte[] compdata)
    {
        InflaterInputStream is = new InflaterInputStream(new ByteArrayInputStream(compdata));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try
        {
            while(is.available() > 0)
            {
                byte[] buffer = new byte[1024];
                int read = is.read(buffer);
                out.write(buffer);
            }
            is.close();
            out.flush();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        byte[] data = out.toByteArray();
        ByteBuffer buf = ByteBuffer.wrap(data);
        int rows = buf.getInt();
        int cols = buf.getInt();
        int type = buf.getInt();
        byte[] img = new byte[buf.remaining()];
        buf.get(img);
        Mat desc = new Mat(rows, cols, type);
        desc.put(0,0,img);
        KeypointDescList list = new KeypointDescList(new MatOfKeyPoint(), desc);
        return super.get(list);
    }

    /**
     * The callback to be called if this device gets a get-request
     */
    public class RequestCallback extends GetRequestCallback
    {
        @Override
        public Result<Byte[]> get(byte[] data)
        {
            Long start = System.currentTimeMillis();
            Result<String> result = remoteget(data);
//            Result<String> result = new Result<>(Double.MIN_VALUE, "None");
            Long end = System.currentTimeMillis() - start;
            System.out.println("Computed Request in " + end);
            return new Result<>(result.confidence, ArrayUtils.toObject(result.value.getBytes()));
        }
    }
}
