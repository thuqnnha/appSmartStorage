package com.example.appsmartstorage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.Manifest;

public class SellingActivity extends Fragment {
    private Button btnScanning;
    private RecyclerView recyclerViewProducts;
    private List<Product> productList = new ArrayList<>();
    private ProductAdapter productAdapter;
    private Button btnPrintBill;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public SellingActivity() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissions();
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_selling_activity, container, false);

        btnScanning = view.findViewById(R.id.btnScanning);
        recyclerViewProducts = view.findViewById(R.id.recyclerViewProducts);

        // Cấu hình RecyclerView
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        productAdapter = new ProductAdapter(productList);
        recyclerViewProducts.setAdapter(productAdapter);

        // Xử lý khi nhấn nút quét QR
        btnScanning.setOnClickListener(v -> startQRScanner());
        //nhấn nút in bill
        btnPrintBill = view.findViewById(R.id.btnPrintBill);
        btnPrintBill.setOnClickListener(v -> {
            executorService.execute(() -> {
                List<String> errorProducts = DatabaseHelper.checkAndUpdateStock(productList);

                requireActivity().runOnUiThread(() -> {
                    if (errorProducts.isEmpty()) {
                        // Tất cả đều đủ → in bill
                        Bitmap billBitmap = createBillBitmap(productList);
                        saveImage(billBitmap);

                        // Clear list
                        productList.clear();
                        productAdapter.notifyDataSetChanged();
                    } else {
                        // Báo lỗi
                        String message = "Không đủ hàng cho: " + String.join(", ", errorProducts);
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    }
                });
            });
        });


        return view;
    }
    private void startQRScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Quét mã QR");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        options.setCaptureActivity(CustomScannerActivity.class); // Tuỳ chỉnh nếu muốn

        barcodeLauncher.launch(options);
    }
    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    String scannedMaVatTu = result.getContents();
                    Toast.makeText(getContext(), "Đã quét: " + scannedMaVatTu, Toast.LENGTH_SHORT).show();

                    // Tìm sản phẩm trong DB
                    new Thread(() -> {
                        Product product = DatabaseHelper.getProduct(scannedMaVatTu);

                        if (product != null) {
                            requireActivity().runOnUiThread(() -> {
                                boolean found = false;

                                // Kiểm tra sản phẩm đã có trong list chưa
                                for (Product p : productList) {
                                    if (p.getMaVatTu().equals(product.getMaVatTu())) {
                                        p.increaseSoLuong(); // tăng số lượng
                                        productAdapter.notifyDataSetChanged(); // cập nhật lại
                                        found = true;
                                        break;
                                    }
                                }

                                if (!found) {
                                    productList.add(product); // Thêm mới sản phẩm
                                    productAdapter.notifyItemInserted(productList.size() - 1);
                                }
                            });
                        } else {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(getContext(), "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                }
            }
    );
    private Bitmap createBillBitmap(List<Product> productList) {
        int width = 800;
        int padding = 40;
        int lineHeight = 70;
        int height = padding * 2 + (productList.size() + 5) * lineHeight;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        canvas.drawColor(Color.WHITE); // Đặt nền trắng cho canvas
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(36);
        paint.setAntiAlias(true);

        int y = padding;

        // Tiêu đề
        paint.setFakeBoldText(true);
        canvas.drawText("HÓA ĐƠN BÁN HÀNG", width / 2 - 150, y, paint);
        y += lineHeight;

        // Tiêu đề bảng
        paint.setFakeBoldText(false);
        canvas.drawText("STT  Tên sản phẩm    SL x Đơn giá    Thành tiền", padding, y, paint);
        y += lineHeight;

        int stt = 1;
        int tongSoLuong = 0;
        double tongTien = 0;

        // Vị trí bắt đầu của các cột
        float sttX = padding;
        float tenVatTuX = sttX + 120;  // Khoảng cách từ STT đến Tên vật tư
        float slX = sttX + 350;  // Khoảng cách từ Tên vật tư đến SL
        float donGiaX = sttX + 400;   // Khoảng cách từ SL đến Đơn giá
        float thanhTienX = sttX + 580; // Khoảng cách từ Đơn giá đến Thành tiền

        for (Product p : productList) {
            int sl = p.getSoLuong();
            double donGia = p.getGiaTien();
            double thanhTien = sl * donGia;

            String sttStr = String.valueOf(stt);
            String tenVatTu = p.getTenVatTu();
            String slStr = String.valueOf(sl);
            String donGiaStr = String.valueOf((int) donGia);
            String thanhTienStr = String.valueOf((int) thanhTien);

            // Vẽ các phần tử vào canvas từ vị trí căn chỉnh của từng cột
            canvas.drawText(sttStr, sttX, y, paint);
            canvas.drawText(tenVatTu, tenVatTuX, y, paint);
            canvas.drawText(slStr, slX, y, paint);
            canvas.drawText(donGiaStr, donGiaX, y, paint);
            canvas.drawText(thanhTienStr, thanhTienX, y, paint);

            y += lineHeight; // Chuyển xuống dòng tiếp theo

            tongSoLuong += sl;
            tongTien += thanhTien;
            stt++;
        }

        // In thông tin tổng
        y += lineHeight / 2;
        canvas.drawText("Tổng số lượng: " + tongSoLuong, padding, y, paint);
        y += lineHeight;
        canvas.drawText("Tổng tiền hàng: " + String.format("%,.0f", tongTien) + " VND", padding, y, paint);
        y += lineHeight;

        double vat = tongTien * 0.1;
        canvas.drawText("VAT (10%): " + String.format("%,.0f", vat) + " VND", padding, y, paint);
        y += lineHeight;

        double tongSauVAT = tongTien + vat;
        paint.setFakeBoldText(true);
        canvas.drawText("TỔNG TIỀN THANH TOÁN: " + String.format("%,.0f", tongSauVAT) + " VND", padding, y, paint);

        return bitmap;
    }
    public void saveImage(Bitmap bitmap) {
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContext().getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "my_image_" + System.currentTimeMillis() + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(imageUri);
            } else {
//                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
//                File image = new File(imagesDir, "MyBill" + System.currentTimeMillis() + ".jpg");
//                fos = new FileOutputStream(image);
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();

                // Format theo MMddHHmmss
                SimpleDateFormat sdf = new SimpleDateFormat("MMddyyHHmmss");
                String currentTime = sdf.format(new Date());

                File image = new File(imagesDir, "MyBill" + currentTime + ".jpg");
                fos = new FileOutputStream(image);
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(getContext(), "Saved successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to save image!", Toast.LENGTH_SHORT).show();
        }
    }



    //    /* Checks if external storage is available for read and write */
//    public boolean isExternalStorageWritable() {
//        String state = Environment.getExternalStorageState();
//        if (Environment.MEDIA_MOUNTED.equals(state)) {
//            return true;
//        }
//        return false;
//    }
//
//    // Kiểm tra và yêu cầu quyền lưu trữ
//    private void checkStoragePermission() {
//        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//        }
//    }
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == 1) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Quyền đã được cấp, có thể lưu ảnh
//                Toast.makeText(getContext(), "Quyền đã được cấp", Toast.LENGTH_SHORT).show();
//            } else {
//                // Quyền bị từ chối, hiển thị thông báo hoặc yêu cầu quyền lại
//                Toast.makeText(getContext(), "Quyền bị từ chối", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33)
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 1);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // Android 6 (API 23)
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "Permission Granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }


}