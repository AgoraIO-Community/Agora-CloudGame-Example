package io.agora.cloudgame.utils;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import io.agora.base.VideoFrame;

public class YuvDumper {
    private static final String TAG = YuvDumper.class.getSimpleName();
    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     */
    private final Deque<VideoFrame.I420Buffer> videoFrames = new LinkedList<VideoFrame.I420Buffer>();
    private Context mCurrentContext = null;
    private String mFilePath = null;
    private String mFileName = null;
    private DumperCallback mDumperCallback = null;


    public YuvDumper(Context ctx, String fileName, DumperCallback callback) {
        mCurrentContext = ctx;
        mFileName = fileName;
        mDumperCallback = callback;
    }

    public void updateFileName(String fileName) {
        mFileName = fileName;
        mFilePath = null;
    }

    public int pushFrame(VideoFrame frame) {
        VideoFrame.I420Buffer buf = frame.getBuffer().toI420();
        if (mFilePath == null) {
            mFilePath = getDatasFilePath(mCurrentContext) + mFileName + "_" + buf.getWidth() + "x" + buf.getHeight() + ".yuv";
        }
        videoFrames.add(buf);
        return 0;
    }

    public int clearFrames() {
        videoFrames.clear();
        return 0;
    }

    public String getDumpFilePath() {
        return mFilePath;
    }

    public void saveToFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(mFilePath);
                Log.i(TAG, "delete=" + file.delete());
                VideoFrame.I420Buffer buffer;
                while (!videoFrames.isEmpty()) {
                    try {
                        buffer = videoFrames.poll();
                        writeYuvToFile(buffer.getDataY(), buffer.getStrideY(), buffer.getWidth(), buffer.getHeight());
                        writeYuvToFile(buffer.getDataU(), buffer.getStrideU(), buffer.getWidth() / 2, buffer.getHeight() / 2);
                        writeYuvToFile(buffer.getDataV(), buffer.getStrideV(), buffer.getWidth() / 2, buffer.getHeight() / 2);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (null != mDumperCallback) {
                    mDumperCallback.onDumpSuccess(mFilePath);
                }
            }
        }).start();
    }

    private void writeYuvToFile(ByteBuffer src, int stride, int width, int height) throws IOException {
        byte[] bV = new byte[width * height];
        byte[] tempBv = new byte[stride - width];
        for (int i = 0; i < height; i++) {
            src = src.get(bV, i * width, width);
            if (src.remaining() >= stride - width) {
                src = src.get(tempBv, 0, stride - width);
            }
        }
        writeBytesToFile(bV);
    }

    /**
     * byte数组存储文件
     *
     * @param bs
     * @throws IOException
     */
    public void writeBytesToFile(byte[] bs) throws IOException {
        Log.e(TAG, "writeBytesToFile bs=" + Arrays.toString(bs));
        OutputStream out = new FileOutputStream(mFilePath, true);
        InputStream is = new ByteArrayInputStream(bs);
        byte[] buff = new byte[1024];
        int len;

        while ((len = is.read(buff)) != -1) {
            out.write(buff, 0, len);
        }

        is.close();
        out.close();

        Log.e(TAG, "writeBytesToFile end");
    }

    /**
     * 获取文件夹路径
     *
     * @param context
     * @return
     */
    private static String getDatasFilePath(Context context) {
        String path = null;
        try {
            path = context.getExternalCacheDir().getPath() + "/";
            File file = new File(path);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e(TAG, "getDatasFilePath: " + path);
        return path;
    }

    public interface DumperCallback {
        void onDumpSuccess(String filePath);
    }
}
