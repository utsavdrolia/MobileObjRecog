package org.crowdcache.mobileobjrecog.cache;

import org.apache.commons.lang3.ArrayUtils;
import org.crowdcache.rpc.CrowdRPC;
import org.crowdcache.rpc.GetRequestCallback;
import org.crowdcache.rpc.GetResponseCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by utsav on 2/19/16.
 */
public class DistObjectRecogCache extends ObjectRecogCache
{
    private final CrowdRPC rpc;
    private final ByteBuffer buffer = ByteBuffer.allocate(80 * 1024);

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
        Long start = System.currentTimeMillis();
        @Override
        public void done(Result<Byte[]> result)
        {
            Long end = System.currentTimeMillis() - start;
            System.out.println("Got response" + new String(ArrayUtils.toPrimitive(result.value)) + " from peer in " + end);
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
     * @param data
     * @return
     */
    protected Result<String> remoteget(byte[] data)
    {
        return super.get(data);
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
