package com.example.appsmartstorage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false); // sử dụng layout tùy chỉnh
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.txtName.setText("Tên vật tư: " + product.getTenVatTu());
        holder.txtMaVatTu.setText("Mã vật tư: "+product.getMaVatTu());
        holder.txtSoLuong.setText("Số lượng: " + product.getSoLuong());
        holder.txtPrice.setText("Giá: " + product.getGiaTien());

        double tongTien = product.getSoLuong() * product.getGiaTien();
        holder.txtTongTien.setText("Tổng tiền: " + String.format("%,.0f", tongTien) + " VND");

        // Xử lý nút xoá
        holder.btnDelete.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                productList.remove(currentPosition);
                notifyItemRemoved(currentPosition);
            }
        });
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtPrice, txtMaVatTu, txtSoLuong, txtTongTien;
        Button btnDelete;
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtMaVatTu = itemView.findViewById(R.id.txtMaVatTu);
            txtSoLuong = itemView.findViewById(R.id.txtSoLuong);
            txtTongTien = itemView.findViewById(R.id.txtTongTien);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}


