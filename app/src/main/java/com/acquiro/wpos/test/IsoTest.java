package com.acquiro.wpos.test;

import android.content.Context;
import android.util.Log;

import com.acquiro.wpos.R;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;

import java.io.IOException;
import java.io.InputStream;

public class IsoTest {

    private static final String TAG = "IsoTest";

    public static void generateIso(Context context,String data) throws IOException, ISOException {
            // Create Packager based on XML that contain DE type
            InputStream txnInputStream = context.getResources().openRawResource(R.raw.purchase_txn);
            GenericPackager packager = new GenericPackager(txnInputStream);

            // Create ISO Message
            ISOMsg isoMsg = new ISOMsg();
            isoMsg.setPackager(packager);
            isoMsg.unpack(data.getBytes());
            logISOMsg(isoMsg);
        }

        public static void logISOMsg(ISOMsg msg) {
            System.out.println("----ISO MESSAGE-----");
            try {
                System.out.println("  MTI : " + msg.getMTI());
                for (int i=1;i<=msg.getMaxField();i++) {
                    if (msg.hasField(i)) {
                        Log.d(TAG, "    Field-"+i+" : "+msg.getString(i));
                        //System.out.println("    Field-"+i+" : "+msg.getString(i));
                    }
                }
            } catch (ISOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("--------------------");
            }

        }
}
