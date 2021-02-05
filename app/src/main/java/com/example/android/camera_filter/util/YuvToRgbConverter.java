package com.example.android.camera_filter.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import java.nio.ByteBuffer;

public class YuvToRgbConverter {
    RenderScript rs;
    ScriptIntrinsicYuvToRGB scriptYuvToRGBS;
    int pixelCount;
    ByteBuffer yuvBuffer;
    Allocation inputAllocation;
    Allocation outputAllocation;

    Rect planeCrop;
    public YuvToRgbConverter(Context context) {
        rs = RenderScript.create(context);
        scriptYuvToRGBS = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        pixelCount = -1;
    }

    public synchronized void yuvToRgb(Image image, Bitmap output){
        if(yuvBuffer == null){
            pixelCount = image.getCropRect().width() * image.getCropRect().height();
            yuvBuffer = ByteBuffer.allocateDirect(
                    pixelCount * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8);
        }

        imageToByteBuffer(image, yuvBuffer);

        if(inputAllocation == null){
            inputAllocation = Allocation.createSized(rs, Element.U8(rs), yuvBuffer.array().length);
        }
        if(outputAllocation == null){
            outputAllocation = Allocation.createFromBitmap(rs, output);
        }

        inputAllocation.copyFrom(yuvBuffer.array());
        scriptYuvToRGBS.setInput(inputAllocation);
        scriptYuvToRGBS.forEach(outputAllocation);
        outputAllocation.copyTo(output);
    }

    void imageToByteBuffer(Image image, ByteBuffer outputBuffer){
        assert image.getFormat() == ImageFormat.YUV_420_888;

        Rect imageCrop = image.getCropRect();
        Image.Plane[] imagePlanes = image.getPlanes();
        byte[] rowData = new byte[imagePlanes[0].getRowStride()];

        for(int i = 0; i < imagePlanes.length; i++) {
            int outputStride = 0;
            int outputOffset = 0;

            switch (i) {
                case 0:
                    outputStride = 1;
                    outputOffset = 0;
                    break;
                case 1:
                    outputStride = 2;
                    outputOffset = pixelCount + 1;
                    break;
                case 2:
                    outputStride = 2;
                    outputOffset = pixelCount;
                default:
                    break;
            }

            ByteBuffer buffer = imagePlanes[i].getBuffer();
            int rowStride = imagePlanes[i].getRowStride();
            int pixelStride = imagePlanes[i].getPixelStride();

            planeCrop = (i == 0) ? imageCrop : new Rect(imageCrop.left / 2, imageCrop.top / 2, imageCrop.right / 2, imageCrop.bottom / 2);


            int planeWidth = planeCrop.width();
            int planeHeight = planeCrop.height();

            buffer.position(rowStride * planeCrop.top + pixelStride * planeCrop.left);
            for (int row = 0; row < planeHeight; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = planeWidth;
                    buffer.get(outputBuffer.array(), outputOffset, length);
                    outputOffset += length;
                } else {
                    length = (planeWidth - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < planeWidth; col++) {
                        outputBuffer.array()[outputOffset] = rowData[col * pixelStride];
                        outputOffset += outputStride;
                    }
                }

                if (row < planeHeight - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
    }
}
