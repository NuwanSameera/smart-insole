package uom.synergen.fyp.smart_insole.gait_analysis.device_list;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;

import uom.synergen.fyp.smart_insole.gait_analysis.android_agent.R;

public class BleDeviceAdapter  extends ArrayAdapter{

        List<BleDeviceModel> modelItems = null;
        Context context;
        public BleDeviceAdapter(Context context, List<BleDeviceModel> resource) {
            super(context, R.layout.ble_device,resource);
            // TODO Auto-generated constructor stub
            this.context = context;
            this.modelItems = resource;
        }

        @Override
       public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.ble_device, parent, false);
            TextView name = (TextView) convertView.findViewById(R.id.textView1);
            CheckBox cb = (CheckBox) convertView.findViewById(R.id.checkBox1);
            name.setText(modelItems.get(position).getName());

            if(modelItems.get(position).isSeleted()) {
                cb.setChecked(true);
            } else {
                cb.setChecked(false);
            }

            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    modelItems.get(position).setSelected(isChecked);
                }
            });

            return convertView;
       }



}