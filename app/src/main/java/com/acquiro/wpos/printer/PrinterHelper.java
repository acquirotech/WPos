package com.acquiro.wpos.printer;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.acquiro.wpos.R;
import com.acquiro.wpos.jni.PrinterInterface;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * 打印操作
 *
 * @author lianyi
 */
public class PrinterHelper {
    private static PrinterHelper _instance;


    private PrinterHelper() {
    }

    synchronized public static PrinterHelper getInstance() {
        if (null == _instance) {
            _instance = new PrinterHelper();
        }
        return _instance;
    }

    /**
     *
     * @param context
     * @return 0 : Success, -1 : Out Of Paper, -2 : Error while Printing
     * @throws PrinterException
     */
    synchronized public int printTestReceipt(Context context) throws PrinterException {
        try {

            int retOpen = PrinterInterface.open();
            if(retOpen<0){
                closePrinter();
                PrinterInterface.open();
            }
            int r = PrinterInterface.queryStatus();
            Log.d("printTestReceipt", "printTestReceipt query 1: " + r);
            if (r == 0) {
                closePrinter();
                return -1; //No Paper
            } else if (r == 1) { //Paper Present
                PrinterInterface.begin();

                printerWrite(PrinterCommand.init());
                printerWrite(PrinterCommand.setHeatTime(180));
                printerWrite(PrinterCommand.setAlignMode(1));
                printerWrite(PrinterCommand.setFontBold(1));
                printerWrite(("SALE").getBytes("GB2312"));
                printerWrite(PrinterCommand.feedLine(2));

                printerWrite(PrinterCommand.setFontBold(0));
                printerWrite(("Visa Debit").getBytes("GB2312"));
                printerWrite(PrinterCommand.linefeed());

                printerWrite(("CARD NUM:xxx xxxx xxxx 6623 CHIP").getBytes("GB2312"));
                printerWrite(PrinterCommand.linefeed());

                printerWrite(("EXP DATE: xx/xx  CARD TYPE: VISA").getBytes("GB2312"));
                printerWrite(PrinterCommand.linefeed());

                printerWrite(("APPR CODE: 124523   RREF NUM: 3434343434").getBytes("GB2312"));
                printerWrite(PrinterCommand.linefeed());

                printerWrite(("AID :A00000031010   TC 23434234232GG234g").getBytes("GB2312"));
                printerWrite(PrinterCommand.linefeed());

                printerWrite(PrinterCommand.feedLine(2));
                printerWrite(PrinterCommand.setFontBold(1));
                printerWrite(("AMOUNT :               100.00").getBytes("GB2312"));

                printerWrite(PrinterCommand.feedLine(2));
                printerWrite(PrinterCommand.setFontBold(0));
                printerWrite(("PIN VERIFIED OK").getBytes("GB2312"));
                printerWrite(PrinterCommand.linefeed());

                printerWrite(PrinterCommand.setFontBold(0));
                printerWrite(("SIGNATURE NOT REQUIRED").getBytes("GB2312"));
                printerWrite(PrinterCommand.linefeed());
                printerWrite("--------------------------------".getBytes("GB2312"));


                printerWrite("Customer Copy".getBytes("GB2312"));
                printerWrite(PrinterCommand.linefeed());

                printerWrite(PrinterCommand.feedLine(5));
                PrinterInterface.end();
                int ret = PrinterInterface.queryStatus();
                Log.d("printTestReceipt", "printTestReceipt query 2: " +ret);
                if (ret == 0) {
                    closePrinter();
                    return -1;
                }
            } else {
                closePrinter();
                return -2;
            }
        } catch (UnsupportedEncodingException e) {
            closePrinter();
            e.printStackTrace();
            return -2; //Exception
        } finally {
            closePrinter();
        }
        return 0;

    }

    private void closePrinter() {
        PrinterInterface.end();
        PrinterInterface.close();
    }


    /*synchronized public void printReceipt(MainApp appState, int receipt) throws PrinterException
    {
		try {
		    PrinterInterface.open();
		    PrinterInterface.begin();
	
		    printerWrite(PrinterCommand.init());
		    printerWrite(PrinterCommand.setHeatTime(180));
		    printerWrite(PrinterCommand.setAlignMode(1));
		    printerWrite(PrinterCommand.setFontBold(1));

		    printerWrite(("POS SLIP").getBytes("GB2312"));
		    printerWrite(PrinterCommand.feedLine(2));
		    printerWrite(PrinterCommand.setAlignMode(0));
		   
		    printerWrite("--------------------------------".getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
		    
		    printerWrite("Merchant Name".getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
			    
	    	if(receipt == 0)
		    {
		    	printerWrite(("MERCHANT COPY").getBytes("GB2312"));
			    printerWrite(PrinterCommand.linefeed());
		    }
		    else if(receipt == 1)
		    {
		    	printerWrite(("CARDHOLDER COPY").getBytes("GB2312"));
		    	
			    printerWrite(PrinterCommand.linefeed());
		    }
		    else if(receipt == 2)
		    {
		    	printerWrite(("BANK COPY").getBytes("GB2312"));
			    printerWrite(PrinterCommand.linefeed());
		    }
		    printerWrite("--------------------------------".getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
		    printerWrite("TID :dsfsdfsdf".getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
		    
		    printerWrite("MID".getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
 
		    String pan = appState.getString(R.string.pan_tag) + " " + appState.trans.getPAN();
		    switch(appState.trans.getCardEntryMode())
		    {
		    case 0:
		    	pan = pan + " N";
		    	break;
		    case Constant.SWIPE_ENTRY:
		    	pan = pan + " S";
		    	break;
		    case Constant.INSERT_ENTRY:
		    	pan = pan + " I";
		    	break;
		    case Constant.MANUAL_ENTRY:
		    	pan = pan + " M";
		    	break;
		    default:
		    	pan = pan + " C";
		    	break;
		    }
		    printerWrite(pan.getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
		    
		    printerWrite((  appState.getString(R.string.date_tag) 
		    		      + " " + appState.trans.getTransDate().substring(0, 4)
		    		      + "/" + appState.trans.getTransDate().substring(4, 6)
		    		      + "/" + appState.trans.getTransDate().substring(6, 8)
		    		      + " " + appState.trans.getTransTime().substring(0, 2)
		    		      + ":" + appState.trans.getTransTime().substring(2, 4)
		    		      + ":" + appState.trans.getTransTime().substring(4, 6) ).getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
		    
		    printerWrite(( "TICKET:" + StringUtil.fillZero(Integer.toString(appState.trans.getTrace()), 6)).getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed()); 
	    
		    printerWrite(appState.getString(TransDefine.transInfo[appState.trans.getTransType()].id_display_en).getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
		    
		    printerWrite(("AMOUNT:" + StringUtil.fillString(AppUtil.formatAmount(appState.trans.getTransAmount()), 22, ' ', true)).getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());

		    if(appState.trans.getCardEntryMode() != Constant.SWIPE_ENTRY)
		    {
			    printerWrite(("CSN:" + StringUtil.fillZero(Byte.toString(appState.trans.getCSN()),2)).getBytes());
			    printerWrite(PrinterCommand.linefeed());
		    	
			    printerWrite(("UNPR NUM:" + appState.trans.getUnpredictableNumber()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
		    	printerWrite(("AC:" + appState.trans.getAC()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("TVR:" + appState.trans.getTVR()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("AID:" + appState.trans.getAID()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("TSI:" + appState.trans.getTSI()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("APPLAB:" + appState.trans.getAppLabel()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("APPNAME:" + appState.trans.getAppName()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("AIP:" + appState.trans.getAIP()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("IAD:" + appState.trans.getIAD()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("TermCap:" + appState.terminalConfig.getTerminalCapabilities()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
		    }
		    if( appState.trans.getNeedSignature() == 1)
		    {
			    String sig = "CARDHOLDER SIGNATURE";
			    printerWrite(sig.getBytes("GB2312")); 
			    printerWrite(PrinterCommand.feedLine(3)); 
			    printerWrite("--------------------------------".getBytes("GB2312"));
			    printerWrite(PrinterCommand.linefeed());
		    }
		    printerWrite(PrinterCommand.feedLine(2));
		    
		} catch (UnsupportedEncodingException e) {
		    throw new PrinterException("PrinterHelper.printReceipt():" + e.getMessage(), e);
		} catch (IllegalArgumentException e) {
		    throw new PrinterException(e.getMessage(), e);
		} finally {
		    PrinterInterface.end();
		    PrinterInterface.close();
		}
    }*/

    public void printerWrite(byte[] data) {
        PrinterInterface.write(data, data.length);
    }
}
