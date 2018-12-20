package rizalalfarizi1600807.cs.upi.edu.uasmobprog2018.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import rizalalfarizi1600807.cs.upi.edu.uasmobprog2018.R;
import rizalalfarizi1600807.cs.upi.edu.uasmobprog2018.model.BrakeDetector;

public class RvAdapter extends RecyclerView.Adapter<RvAdapter.CategoryViewHolder>{

    private Context context;
    ArrayList<BrakeDetector> getListSensor() {
        return listSensor;
    }

    private ArrayList<BrakeDetector> listSensor;
    public RvAdapter(Context context) {
        this.context = context;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_detector, parent, false);
        return new CategoryViewHolder(itemRow);
    }
    @Override
    public void onBindViewHolder(CategoryViewHolder holder, int position) {


        if(getListSensor().get(position).lattitude==0){
            holder.tvLatlong.setText(getListSensor().get(position).latlong);
        }else{
            holder.tvLatlong.setText("Lat:" + getListSensor().get(position).lattitude + ";Long:"+getListSensor().get(position).longitude);
        }

        holder.tvDate.setText(getListSensor().get(position).date);
        holder.tvStatus.setText(getListSensor().get(position).brake_status);
    }
    @Override
    public int getItemCount() {
        return getListSensor().size();
    }

    public void setListSensor(ArrayList<BrakeDetector> listSensor) {
        this.listSensor = listSensor;
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder{
        TextView tvDate,tvLatlong,tvStatus;
        CategoryViewHolder(View itemView) {
            super(itemView);

            tvDate = (TextView)itemView.findViewById(R.id.tv_date);
            tvLatlong = (TextView)itemView.findViewById(R.id.tv_latlong);
            tvStatus = (TextView)itemView.findViewById(R.id.tv_status);
        }
    }
}