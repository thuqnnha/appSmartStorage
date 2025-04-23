package com.example.appsmartstorage;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StoringActivity extends Fragment {
    private TableLayout tableLayout;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private DatabaseHelper databaseHelper;
    private Button btnNewScanning;
    private Button btnCurrentScanning;
    public StoringActivity() {
        // Constructor rỗng
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_storing, container, false);
        tableLayout = view.findViewById(R.id.tableLayout);

        // Khởi tạo DatabaseHelper
        databaseHelper = new DatabaseHelper(requireContext());

        // Tải dữ liệu từ Database
        fetchDataFromDatabase();
        //quét qr
        btnNewScanning = view.findViewById(R.id.btnNewScaning);
        btnNewScanning.setOnClickListener(v -> startQRScanner());

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
private String maVatTu;
    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (result.getContents() != null) {
                    Toast.makeText(getContext(), result.getContents(), Toast.LENGTH_SHORT).show();
                    maVatTu = result.getContents();
                    showEditAccountDialog();
                    try {
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.encodeBitmap(result.getContents(), com.google.zxing.BarcodeFormat.QR_CODE, 200, 200);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
    );
    public void showEditAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm sản phẩm mới");

        // Inflate view
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_newproduct, null);
        builder.setView(viewInflated);

        EditText edtIdStore = viewInflated.findViewById(R.id.edtIdStore);
        EditText edtName = viewInflated.findViewById(R.id.edtName);
        EditText edtUnit = viewInflated.findViewById(R.id.edtUnit);
        EditText edtLocation = viewInflated.findViewById(R.id.edtLocation);
        EditText edtCount = viewInflated.findViewById(R.id.edtCount);
        EditText edtPrice = viewInflated.findViewById(R.id.edtPrice);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String idStore = edtIdStore.getText().toString().trim();
            String Name = edtName.getText().toString().trim();
            String Unit = edtUnit.getText().toString().trim();
            String Location = edtLocation.getText().toString().trim();
            String Count = edtCount.getText().toString().trim();
            String Price = edtPrice.getText().toString().trim();

            if (idStore.isEmpty() || Name.isEmpty() || Unit.isEmpty() || Location.isEmpty() || Count.isEmpty() || Price.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            executorService.execute(() -> {
                boolean success = DatabaseHelper.updateData(idStore,maVatTu,Name,Unit,Location,Count,Price);
                requireActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getContext(), "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi thêm sản phẩm!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
    // Lấy dữ liệu từ Database
    private void fetchDataFromDatabase() {
        executorService.execute(() -> {
            List<String[]> data = databaseHelper.getDataFromDatabase();

            requireActivity().runOnUiThread(() -> {
                if (data.isEmpty()) {
                    Toast.makeText(getContext(), "Không có dữ liệu hoặc lỗi kết nối!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Tải dữ liệu thành công!", Toast.LENGTH_SHORT).show();
                    displayData(data);
                }
            });
        });
    }

    // Hiển thị dữ liệu trong TableLayout
    private void displayData(List<String[]> data) {
        tableLayout.removeAllViews();

        // Thêm tiêu đề bảng
        TableRow headerRow = new TableRow(getContext());
        String[] headers = {"Mã Kho", "Mã Vật Tư", "Tên Vật Tư", "Đơn Vị", "Vị Trí", "SL Nhập", "Thời Gian Nhập", "Giá Tiền"};
        for (String header : headers) {
            headerRow.addView(createTextView(header, true));
        }
        tableLayout.addView(headerRow);

        // Thêm dữ liệu
        for (String[] rowData : data) {
            TableRow row = new TableRow(getContext());
            for (String cellData : rowData) {
                row.addView(createTextView(cellData, false));
            }
            tableLayout.addView(row);
        }
    }

    // Tạo TextView cho TableLayout
    private TextView createTextView(String text, boolean isHeader) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setPadding(20, 10, 20, 10);
        textView.setTextSize(isHeader ? 16 : 14);
        textView.setTextColor(ContextCompat.getColor(requireContext(), isHeader ? android.R.color.black : android.R.color.darker_gray));
        textView.setBackgroundColor(ContextCompat.getColor(requireContext(), isHeader ? android.R.color.holo_blue_light : android.R.color.white));
        textView.setGravity(Gravity.CENTER);
        return textView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executorService.shutdown();
    }
}
