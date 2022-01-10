package com.kingsbyte.subrefill;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionVH> {
    private Context context;
    private List<TransactionObject> list;

    public TransactionAdapter(Context context, List<TransactionObject> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public TransactionVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.transaction_item,parent,false);
        return new TransactionVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionVH holder, int position) {
        TransactionObject obj = list.get(position);
        String date = obj.getDate();
        String status = obj.getStatus();
        String serviceType = obj.getTransactionType();
        String price = obj.getPrice();
        String accountNumber = obj.getAccountNumber();
        String phone = obj.getPhone();
        String type = obj.getType();
        String tokenCode = obj.getTokenCode();
        try {
            serviceType = serviceType.substring(0, 1).toUpperCase() + "" + serviceType.substring(1);
        }catch (StringIndexOutOfBoundsException e){
            e.printStackTrace();
        }
        OffsetDateTime inst = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            inst = OffsetDateTime.ofInstant(Instant.parse(date),
                    ZoneId.systemDefault());
            String newDate = DateTimeFormatter.ofPattern("MMM dd, yyyy").format(inst);
            holder.dateTXT.setText(newDate);

        }

        NumberFormat formatter= new DecimalFormat("#,###");
        price= formatter.format(Double.parseDouble(price));
        Log.i("response","token "+tokenCode);
        if(serviceType.equalsIgnoreCase("Payment")){
            holder.transactionTypeTXT.setText(serviceType);
        }else if(!accountNumber.equals("null")) {
            holder.transactionTypeTXT.setText(serviceType + "(" + accountNumber + ")");
        }else if(!phone.equals("null")){
            holder.transactionTypeTXT.setText(serviceType + "(" + phone+ ")");
        }else{
            holder.transactionTypeTXT.setText(serviceType);
        }
        holder.priceTXT.setText("₦" +price);
        holder.statusTXT.setText(status);
        if(!tokenCode.equalsIgnoreCase("null") ){
            holder.tokenCodeTxt.setVisibility(View.VISIBLE);
            holder.tokenCodeTxt.setText("Token: "+tokenCode);
        }else {
            holder.tokenCodeTxt.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class TransactionVH extends RecyclerView.ViewHolder{
        TextView statusTXT,priceTXT,dateTXT,transactionTypeTXT,tokenCodeTxt;
        ImageView icon;
        public TransactionVH(@NonNull View itemView) {
            super(itemView);
            statusTXT = itemView.findViewById(R.id.status);
            priceTXT = itemView.findViewById(R.id.price);
            dateTXT = itemView.findViewById(R.id.date);
            transactionTypeTXT = itemView.findViewById(R.id.transaction_type);
            icon = itemView.findViewById(R.id.icon);
            tokenCodeTxt = itemView.findViewById(R.id.token_code);
        }
    }
}
