package uom.synergen.fyp.smart_insole.gait_analysis.device_list;

public class BleDeviceModel {

    private String name;
    private boolean select;

    public BleDeviceModel(String name){
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean isSeleted(){
        return this.select;
    }

    public void setSelected(boolean select) {
        this.select = select;
    }

    @Override
    public boolean equals(Object obj) {
        BleDeviceModel bleDeviceModel = (BleDeviceModel) obj;
        return bleDeviceModel.name.equals(this.name);
    }

}
