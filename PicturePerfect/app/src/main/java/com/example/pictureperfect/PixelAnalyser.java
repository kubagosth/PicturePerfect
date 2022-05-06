package com.example.pictureperfect;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class PixelAnalyser extends Thread {

    //Testing
    int removed = 0;

    private final ImageView imageView;
    private final Bitmap bitmap;
    private boolean threadIsRunning;
    private final int h;
    private final int w;
    private final ArrayList<RGB> rgbGroup;
    private final ArrayList<Color> color;

    public ArrayList<RGB> getRgbGroup() {
        return rgbGroup;
    }

    public boolean isThreadIsRunning() {
        return threadIsRunning;
    }

    /**
     * //Constructor
     * @param imageView - Image to analyse
     */
    public PixelAnalyser(ImageView imageView)
    {
        this.imageView = imageView;
        bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        h = bitmap.getHeight();
        w = bitmap.getWidth();
        rgbGroup =  new ArrayList<RGB>();
        color = new ArrayList<Color>();
    }

    /**
     * //Create a thread
     * @param yStart - Start for getting pixels
     * @param yFinish - Finish for getting pixels
     * @return - thread that is ready to run
     * //API 29 and up - getPixel()
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Thread getPixelThread(int yStart,int yFinish)
    {
        Thread thread = new Thread(new Thread() {
            @Override
            public void run() {
                getPixel(yStart,yFinish);
                Log.d("Log-" + this.getName(),yStart + " | " + yFinish);
            }
        });
        return thread;
    }

    /**
     *  Pixel Analyser thread
     *  //API 29 and up - getPixelThread() - groupColor() Include methods
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void run()
    {
        threadIsRunning = true;

        Thread th1 = getPixelThread(0,h/2);
        Thread th2 = getPixelThread(h/2,h);

        th1.start();
        th2.start();

        boolean threads = false;
        while (!threads)
        {
            if (!th1.isAlive() && !th2.isAlive())
                threads = true;
        }
        Log.d("Log-PixelAnalyser-Thread", "Total Pixels : " + color.size());
        groupColor(color);
        threadIsRunning = false;
    }

    /**
     * Find the color difference
     * Compare two colors return distance
     * @param rgb - color 1
     * @param rgb2 - color 2
     * @return - color range distance
     */
    public double colorRange(RGB rgb, RGB rgb2)
    {
       double red = (rgb2.getRed() - rgb.getRed()) * (rgb2.getRed() - rgb.getRed());
       double green = (rgb2.getGreen() - rgb.getGreen()) * (rgb2.getGreen() - rgb.getGreen());
       double blue = (rgb2.getBlue() - rgb.getBlue()) * (rgb2.getBlue() - rgb.getBlue());
       return Math.sqrt(red+green+blue);
    }
    /**
     * //Get Pixels from bitmap
     * @param yStart - start position
     * @param yFinish - end position
     * //API 29 and up - getColor()
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void getPixel(int yStart, int yFinish)
    {
            for (int y = yStart; y < yFinish; y++) {
                for (int x = 0; x < w; x++) {
                    try {
                        synchronized(this)
                        {
                            color.add(bitmap.getColor(x,y));
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d("Log-E",e.toString());
                    }
                }
            }
    }


    /**
     * Remove Black and White colors in range of 30
     * Fill the hashmap with colors
     * @param hashMapRGB - Empty hashmap
     * @param colorArrayList - List of all colors
     * @return - Hashmap with Store colors with a value of false which mean unsorted
     * //Api29 and up - red() - green() - blue()
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public HashMap<RGB,Boolean> removeBlackAndWhite(HashMap<RGB,Boolean> hashMapRGB, ArrayList<Color> colorArrayList)
    {
        for (Color item:colorArrayList)
        {
            try {
                //Get the distance to white and black
                RGB temp = new RGB((int) (item.red()*100),(int)(item.green()*100),(int)(item.blue()*100));
                double white = colorRange(temp,new RGB(255,255,255));
                double black = colorRange(temp,new RGB(0,0,0));
                if (white > 30 && black > 30)
                {
                    hashMapRGB.put(temp,false);
                }
                else
                    //Count deleted - For testing
                    removed++;
            }
            catch (NullPointerException e)
            {
                Log.d("Log-E","item is null");
            }
        }
        return hashMapRGB;
    }

    /**
     * Group Colors by color distance of 10
     * @param colorArrayList list of all colors
     * //Api29 and up - removeBlackAndWhite() Include method
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void groupColor(ArrayList<Color> colorArrayList)
    {
        HashMap<RGB,Boolean> hashMapRGB = new HashMap<>();
        hashMapRGB = removeBlackAndWhite(hashMapRGB,colorArrayList);
        //Color range
        RGB tempRGB;
        for (Map.Entry<RGB,Boolean> val : hashMapRGB.entrySet())
        {
            //If no colors in the group
            if (rgbGroup.size() == 0)
            {
                tempRGB = val.getKey();
                tempRGB.countUp();
                rgbGroup.add(tempRGB);
                val.setValue(true);
            }
            else
            {
                //Check if fit into any group
                for (RGB rgb:rgbGroup)
                {
                    if (colorRange(rgb,val.getKey()) < 10 && val.getValue() == false)
                    {
                        val.setValue(true);
                        rgb.countUp();
                    }
                }
                //If not in group and not sorted yet add as new group
                if (!rgbGroup.contains(val.getKey()) && val.getValue() == false)
                {
                    tempRGB = val.getKey();
                    tempRGB.countUp();
                    rgbGroup.add(tempRGB);
                    val.setValue(true);
                }
            }
        }

        sortArrayListDescending();
        Log.d("Log","Info\n" +
                "\n---------------------------" +
                "\nTotal groups :" + rgbGroup.size() +
                "\nTotal amount of colors : " + colorArrayList.size() +
                "\nHashMap Size : " + hashMapRGB.size() +
                "\nRemoved from Hashmap :" + removed +
                "\n---------------------------");
    }

    /**
     * Sort array by count descending
     */
    public void sortArrayListDescending()
    {
        Collections.sort(rgbGroup, new Comparator<RGB>() {
            @Override
            public int compare(RGB rgb1, RGB rgb2) {
                return Integer.valueOf(rgb2.getCount()).compareTo(rgb1.getCount());
            }
        });
    }
}