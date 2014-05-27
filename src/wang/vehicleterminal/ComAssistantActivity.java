package wang.vehicleterminal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import wang.serial.bean.AssistBean;
import wang.serial.bean.ComBean;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Bundle;
import android.serialport.api.MyFunc;
import android.serialport.api.SerialHelper;
import android.serialport.api.SerialPortFinder;
import android.text.InputType;
import android.text.method.KeyListener;
import android.text.method.NumberKeyListener;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


public class ComAssistantActivity extends Activity {
	EditText editTextRecDisp,editTextLines,editTextCOMA;
	EditText editTextTimeCOMA;
	CheckBox checkBoxAutoClear,checkBoxAutoCOMA;
	Button ButtonClear,ButtonSendCOMA;
	ToggleButton toggleButtonCOMA;
	Spinner SpinnerCOMA;
	Spinner SpinnerBaudRateCOMA;
	RadioButton radioButtonTxt,radioButtonHex;
	SerialControl ComA;
	DispQueueThread DispQueue;//ˢ����ʾ�߳�
	SerialPortFinder mSerialPortFinder;//列表串口设备
	AssistBean AssistData;//助手信息
	int iRecLines=0;//
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comassistant);
        ComA = new SerialControl();
   
        DispQueue = new DispQueueThread();
        DispQueue.start();
        AssistData = getAssistData();
        setControls();
    }
    @Override
    public void onDestroy(){
    	saveAssistData(AssistData);
    	CloseComPort(ComA);
    	super.onDestroy();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      CloseComPort(ComA);
      setContentView(R.layout.activity_comassistant);
      setControls();
    }
    
    //----------------------------------------------------
    private void setControls()
	{
    	String appName = getString(R.string.app_name);
        try {
			PackageInfo pinfo = getPackageManager().getPackageInfo("com.bjw.ComAssistant", PackageManager.GET_CONFIGURATIONS);
			String versionName = pinfo.versionName;
 			setTitle(appName+" V"+versionName);
        } catch (NameNotFoundException e) {
        	e.printStackTrace();
        }
    	editTextRecDisp=(EditText)findViewById(R.id.editTextRecDisp);
    	editTextLines=(EditText)findViewById(R.id.editTextLines);
    	
    	editTextCOMA=(EditText)findViewById(R.id.editTextCOMA);
    
    	editTextTimeCOMA = (EditText)findViewById(R.id.editTextTimeCOMA);
		
    	checkBoxAutoClear=(CheckBox)findViewById(R.id.checkBoxAutoClear);
		checkBoxAutoCOMA=(CheckBox)findViewById(R.id.checkBoxAutoCOMA);
	
    	ButtonClear=(Button)findViewById(R.id.ButtonClear);
    	ButtonSendCOMA=(Button)findViewById(R.id.ButtonSendCOMA);
    	
    	toggleButtonCOMA=(ToggleButton)findViewById(R.id.toggleButtonCOMA);
      	
    	SpinnerCOMA=(Spinner)findViewById(R.id.SpinnerCOMA);
    	
    	SpinnerBaudRateCOMA=(Spinner)findViewById(R.id.SpinnerBaudRateCOMA);
     	radioButtonTxt=(RadioButton)findViewById(R.id.radioButtonTxt);
    	radioButtonHex=(RadioButton)findViewById(R.id.radioButtonHex);
    	
    	editTextCOMA.setOnEditorActionListener(new EditorActionEvent());
		editTextTimeCOMA.setOnEditorActionListener(new EditorActionEvent());
		editTextCOMA.setOnFocusChangeListener(new FocusChangeEvent());
		editTextTimeCOMA.setOnFocusChangeListener(new FocusChangeEvent());

    	radioButtonTxt.setOnClickListener(new radioButtonClickEvent());
    	radioButtonHex.setOnClickListener(new radioButtonClickEvent());
    	ButtonClear.setOnClickListener(new ButtonClickEvent());
    	ButtonSendCOMA.setOnClickListener(new ButtonClickEvent());
    	toggleButtonCOMA.setOnCheckedChangeListener(new ToggleButtonCheckedChangeEvent());
       	checkBoxAutoCOMA.setOnCheckedChangeListener(new CheckBoxChangeEvent());
    	
    	ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, 
    			R.array.baudrates_value,android.R.layout.simple_spinner_item);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	SpinnerBaudRateCOMA.setAdapter(adapter);
    	
    	SpinnerBaudRateCOMA.setSelection(12);
    	
    	
    	mSerialPortFinder= new SerialPortFinder();
    	String[] entryValues = mSerialPortFinder.getAllDevicesPath();
    	List<String> allDevices = new ArrayList<String>();
		for (int i = 0; i < entryValues.length; i++) {
			allDevices.add(entryValues[i]);
		}
		ArrayAdapter<String> aspnDevices = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, allDevices);
		aspnDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SpinnerCOMA.setAdapter(aspnDevices);
		
		if (allDevices.size()>0)
		{
			SpinnerCOMA.setSelection(0);
		}
	
		SpinnerCOMA.setOnItemSelectedListener(new ItemSelectedEvent());
		SpinnerBaudRateCOMA.setOnItemSelectedListener(new ItemSelectedEvent());
		DispAssistData(AssistData);
	}
    //------------------------
    class ItemSelectedEvent implements Spinner.OnItemSelectedListener{
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
		{
			if ((arg0 == SpinnerCOMA) || (arg0 == SpinnerBaudRateCOMA))
			{
				CloseComPort(ComA);
				checkBoxAutoCOMA.setChecked(false);
				toggleButtonCOMA.setChecked(false);
			}
		}

		public void onNothingSelected(AdapterView<?> arg0)
		{}
    	
    }
    //-----------------
    class FocusChangeEvent implements EditText.OnFocusChangeListener{
		public void onFocusChange(View v, boolean hasFocus)
		{
			if (v==editTextCOMA)
			{
				setSendData(editTextCOMA);
			} else if (v==editTextTimeCOMA)
			{
				setDelayTime(editTextTimeCOMA);
			}
		}
    }
    //----------------
    class EditorActionEvent implements EditText.OnEditorActionListener{
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
		{
			if (v==editTextCOMA)
			{
				setSendData(editTextCOMA);
			} else if (v==editTextTimeCOMA)
			{
				setDelayTime(editTextTimeCOMA);
			}
			return false;
		}
    }
    //-------------------------------
    class radioButtonClickEvent implements RadioButton.OnClickListener{
		public void onClick(View v)
		{
			if (v==radioButtonTxt)
			{
				KeyListener TxtkeyListener = new TextKeyListener(Capitalize.NONE, false);
				editTextCOMA.setKeyListener(TxtkeyListener);
				AssistData.setTxtMode(true);
			}else if (v==radioButtonHex) {
				KeyListener HexkeyListener = new NumberKeyListener()
				{
					public int getInputType()
					{
						return InputType.TYPE_CLASS_TEXT;
					}
					@Override
					protected char[] getAcceptedChars()
					{
						return new char[]{'0','1','2','3','4','5','6','7','8','9',
								'a','b','c','d','e','f','A','B','C','D','E','F'};
					}
				};
				editTextCOMA.setKeyListener(HexkeyListener);
				AssistData.setTxtMode(false);
			}
			editTextCOMA.setText(AssistData.getSendA());
			setSendData(editTextCOMA);
		}
    }
    //----------------------------------------------------�Զ�����
    class CheckBoxChangeEvent implements CheckBox.OnCheckedChangeListener{
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (buttonView == checkBoxAutoCOMA){
				if (!toggleButtonCOMA.isChecked() && isChecked)
				{
					buttonView.setChecked(false);
					return;
				}
				SetLoopData(ComA,editTextCOMA.getText().toString());
				SetAutoSend(ComA,isChecked);
			}
		}
    }
    //----------------------------------------------------���ť�����Ͱ�ť
    class ButtonClickEvent implements View.OnClickListener {
		public void onClick(View v)
		{
			if (v == ButtonClear){
				editTextRecDisp.setText("");
			} else if (v== ButtonSendCOMA){
				sendPortData(ComA, editTextCOMA.getText().toString());
			}
		}
    }
    //--------------------- 
    class ToggleButtonCheckedChangeEvent implements ToggleButton.OnCheckedChangeListener{
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			if (buttonView == toggleButtonCOMA){
				if (isChecked){
					
						ComA.setPort(SpinnerCOMA.getSelectedItem().toString());
						ComA.setBaudRate(SpinnerBaudRateCOMA.getSelectedItem().toString());
						OpenComPort(ComA);
				}else {
					CloseComPort(ComA);
					checkBoxAutoCOMA.setChecked(false);
				}

			}
		}
    }

    private class SerialControl extends SerialHelper{

		public SerialControl(){
		}

		@Override
		protected void onDataReceived(final ComBean ComRecData)
		{
		
			DispQueue.AddQueue(ComRecData);
		
		}
    }
    //--
    private  class DispQueueThread extends Thread{
		private Queue<ComBean> QueueList = new LinkedList<ComBean>(); 
		@Override
		public void run() {
			super.run();
			while(!isInterrupted()) {
				final ComBean ComData;
		        while((ComData=QueueList.poll())!=null)
		        {
		        	runOnUiThread(new Runnable()
					{
						public void run()
						{
							DispRecData(ComData);
						}
					});
		        	try
					{
		        		Thread.sleep(100);//��ʾ���ܸߵĻ������԰Ѵ���ֵ��С��
					} catch (Exception e)
					{
						e.printStackTrace();
					}
		        	break;
				}
			}
		}

		public synchronized void AddQueue(ComBean ComData){
			QueueList.add(ComData);
		}
	}
    //----------------------------------------------------ˢ�½������
    private void DispAssistData(AssistBean AssistData)
	{
    	editTextCOMA.setText(AssistData.getSendA());
   
    	setSendData(editTextCOMA);
    
    	if (AssistData.isTxt())
		{
			radioButtonTxt.setChecked(true);
		} else
		{
			radioButtonHex.setChecked(true);
		}
    	editTextTimeCOMA.setText(AssistData.sTimeA);
  
    	setDelayTime(editTextTimeCOMA);
    
	}
    //----------------------------------------------------���桢��ȡ�������
    private void saveAssistData(AssistBean AssistData) { 
    	AssistData.sTimeA = editTextTimeCOMA.getText().toString();
    
    	SharedPreferences msharedPreferences = getSharedPreferences("ComAssistant", Context.MODE_PRIVATE);
        try {  
            ByteArrayOutputStream baos = new ByteArrayOutputStream();  
            ObjectOutputStream oos = new ObjectOutputStream(baos);  
            oos.writeObject(AssistData); 
            String sBase64 = new String(Base64.encode(baos.toByteArray(),0)); 
            SharedPreferences.Editor editor = msharedPreferences.edit();  
            editor.putString("AssistData", sBase64);  
            editor.commit();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
    //----------------------------------------------------
    private AssistBean getAssistData() {  
    	SharedPreferences msharedPreferences = getSharedPreferences("ComAssistant", Context.MODE_PRIVATE);
    	AssistBean AssistData =	new AssistBean();
        try {  
            String personBase64 = msharedPreferences.getString("AssistData", "");  
            byte[] base64Bytes = Base64.decode(personBase64.getBytes(),0);  
            ByteArrayInputStream bais = new ByteArrayInputStream(base64Bytes);  
            ObjectInputStream ois = new ObjectInputStream(bais);  
            AssistData = (AssistBean) ois.readObject();
            return AssistData;
        } catch (Exception e) {  
            e.printStackTrace();  
        }
		return AssistData;  
    }  
    //----------------------------------------------------�����Զ�������ʱ
    private void setDelayTime(TextView v){
    	if (v==editTextTimeCOMA)
		{
			AssistData.sTimeA = v.getText().toString();
			SetiDelayTime(ComA, v.getText().toString());
		}
    }
    //----------------------------------------------------�����Զ��������
    private void setSendData(TextView v){
    	if (v==editTextCOMA)
		{
			AssistData.setSendA(v.getText().toString());
			SetLoopData(ComA, v.getText().toString());
		} 
    }
    //-------------------------------------------------
    private void SetiDelayTime(SerialHelper ComPort,String sTime){
    	ComPort.setiDelay(Integer.parseInt(sTime));
    }
    //------------------------------------------------
    private void SetLoopData(SerialHelper ComPort,String sLoopData){
    	if (radioButtonTxt.isChecked())
		{
			ComPort.setTxtLoopData(sLoopData);
		} else if (radioButtonHex.isChecked())
		{
			ComPort.setHexLoopData(sLoopData);
		}
    }
    //-----------------------------------------------
    private void DispRecData(ComBean ComRecData){
    	StringBuilder sMsg=new StringBuilder();
    	sMsg.append(ComRecData.sRecTime);
    	sMsg.append("[");
    	sMsg.append(ComRecData.sComPort);
    	sMsg.append("]");
    	if (radioButtonTxt.isChecked())
		{
			sMsg.append("[Txt] ");
			sMsg.append(new String(ComRecData.bRec));
		}else if (radioButtonHex.isChecked()) {
			sMsg.append("[Hex] ");
			sMsg.append(MyFunc.ByteArrToHex(ComRecData.bRec));
		}
    	sMsg.append("\r\n");
    	editTextRecDisp.append(sMsg);
    	iRecLines++;
    	editTextLines.setText(String.valueOf(iRecLines));
    	if ((iRecLines > 500) && (checkBoxAutoClear.isChecked()))//�ﵽ500���Զ����
		{
    		editTextRecDisp.setText("");
    		editTextLines.setText("0");
    		iRecLines=0;
		}
    }
    //----------------------------------------------------�����Զ�����ģʽ����
    private void SetAutoSend(SerialHelper ComPort,boolean isAutoSend){
    	if (isAutoSend)
		{
    		ComPort.startSend();
		} else
		{
			ComPort.stopSend();
		}
    }
    //----------------------------------------------------���ڷ���
    private void sendPortData(SerialHelper ComPort,String sOut){
    	if (ComPort!=null && ComPort.isOpen())
		{
    		if (radioButtonTxt.isChecked())
			{
				ComPort.sendTxt(sOut);
			}else if (radioButtonHex.isChecked()) {
				ComPort.sendHex(sOut);
			}
		}
    }

    private void CloseComPort(SerialHelper ComPort){
    	if (ComPort!=null){
    		ComPort.stopSend();
    		ComPort.close();
		}
    }

    private void OpenComPort(SerialHelper ComPort){
    	try
		{
			ComPort.open();
		} catch (SecurityException e) {
			ShowMessage("打开串口失败:没有串口读/写权限!");
		} catch (IOException e) {
			ShowMessage("打开串口失败:未知错误!");
		} catch (InvalidParameterException e) {
			ShowMessage("打开串口失败:参数错误!");
		}
    }
    //- 
  	private void ShowMessage(String sMsg)
  	{
  		Toast.makeText(ComAssistantActivity.this, sMsg, Toast.LENGTH_SHORT).show();
  	}
}