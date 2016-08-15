package org.zywx.wbpalmstar.plugin.uexscrawl;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BDebug;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.DataHelper;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.plugin.uexscrawl.vo.OpenVO;

public class EUExScrawl extends EUExBase {

    private int mCallbackId=-1;

    public static final int CODE_REQUEST=2;

    public EUExScrawl(Context context, EBrowserView eBrowserView) {
        super(context, eBrowserView);
    }

    @Override
    protected boolean clean() {
        return false;
    }
    

    public void open(String[] params) {
        if (params == null || params.length < 1) {
            errorCallback(0, 0, "error params!");
            return;
        }
        if (params.length>1){
            mCallbackId= Integer.parseInt(params[1]);
        }
        String json = params[0];
        OpenVO openVO= DataHelper.gson.fromJson(json,OpenVO.class);
        String realUrl= BUtility.getRealPathWithCopyRes(mBrwView,openVO.getSrc());
        Intent intent=new Intent(mContext,PhotoScrawlActivity.class);
        intent.putExtra(PhotoScrawlActivity.KEY_INTENT_IMAGE_PATH,realUrl);
        startActivityForResult(intent,CODE_REQUEST);

    }

    private void callBackPluginJs(String methodName, String jsonData){
        String js = SCRIPT_HEADER + "if(" + methodName + "){"
                + methodName + "('" + jsonData + "');}";
        onCallback(js);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode== Activity.RESULT_OK&&requestCode==CODE_REQUEST){
            String savePath=data.getStringExtra(PhotoScrawlActivity.KEY_INTENT_IMAGE_PATH);

            JSONObject jsonResult = new JSONObject();
            try {
                jsonResult.put("savePath", savePath);
            } catch (JSONException e) {
                if (BDebug.DEBUG){
                    e.printStackTrace();
                }
            }
            if (mCallbackId!=-1){
               callbackToJs(mCallbackId,false,0,savePath);
            }else{
                callBackPluginJs(JsConst.CALLBACK_OPEN, jsonResult.toString());
            }
        }else{
            if (mCallbackId!=-1){
                callbackToJs(mCallbackId,false,1);
            }else{
                callBackPluginJs(JsConst.CALLBACK_OPEN, null);
            }
        }
    }
}
