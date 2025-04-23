package com.example.appsmartstorage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AccountUserActivity extends Fragment {
    private ListView listViewAccounts;
    private UserAdapter adapter;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Button btnAddAccount;
    private Button btnDeleteAccount;
    private List<UserAccount> selectedUsers = new ArrayList<>(); // Danh sách tài khoản được chọn

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_user, container, false);

        listViewAccounts = view.findViewById(R.id.listViewAccounts);
        fetchUsers();
        btnAddAccount = view.findViewById(R.id.btnAddAccount);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccounts); // Thêm nút Xóa
        //event
        btnAddAccount.setOnClickListener(v -> showAddAccountDialog());
        btnDeleteAccount.setOnClickListener(v -> deleteSelectedUsers()); // Xử lý xóa tài khoản
        return view;
    }

    private void fetchUsers() {
        executorService.execute(() -> {
            List<UserAccount> userList = DatabaseHelper.getUserAccounts();

            requireActivity().runOnUiThread(() -> {
                if (userList != null && !userList.isEmpty()) {
                    selectedUsers = new ArrayList<>(); // Khởi tạo danh sách tài khoản được chọn
                    adapter = new UserAdapter(AccountUserActivity.this, userList,selectedUsers);//suwar
                    listViewAccounts.setAdapter(adapter);
                } else {
                    //Toast.makeText(requireContext(), "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                    //suwar
                    if (requireActivity() != null) {
                        Toast.makeText(requireActivity(), "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
    private void showAddAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Thêm tài khoản");

        // Tạo view custom cho dialog
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_account, null);
        builder.setView(viewInflated);

        EditText edtUsername = viewInflated.findViewById(R.id.edtUsername);
        EditText edtPassword = viewInflated.findViewById(R.id.edtPassword);
        EditText edtPhone = viewInflated.findViewById(R.id.edtPhone);
        EditText edtEmail = viewInflated.findViewById(R.id.edtEmail);

        builder.setPositiveButton("Thêm", (dialog, which) -> {
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            int idRole = 2; // Mặc định là 2

            if (username.isEmpty() || password.isEmpty() || phone.isEmpty() || email.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            executorService.execute(() -> {
                boolean success = DatabaseHelper.insertUser(username, password, phone, email);
                requireActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getContext(), "Thêm tài khoản thành công!", Toast.LENGTH_SHORT).show();
                        fetchUsers(); // Cập nhật danh sách tài khoản
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi thêm tài khoản!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });


        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
    public void showEditAccountDialog(UserAccount user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Sửa tài khoản");

        // Inflate view
        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_account, null);
        builder.setView(viewInflated);

        EditText edtUsername = viewInflated.findViewById(R.id.edtUsername);
        EditText edtPassword = viewInflated.findViewById(R.id.edtPassword);
        EditText edtPhone = viewInflated.findViewById(R.id.edtPhone);
        EditText edtEmail = viewInflated.findViewById(R.id.edtEmail);

        // Set dữ liệu tài khoản cần sửa
        edtUsername.setText(user.getUsername());
        edtPassword.setText(user.getPassword());
        edtPhone.setText(user.getPhone());
        edtEmail.setText(user.getEmail());

        builder.setPositiveButton("Sửa", (dialog, which) -> {
            String newUsername = edtUsername.getText().toString().trim();
            String newPassword = edtPassword.getText().toString().trim();
            String newPhone = edtPhone.getText().toString().trim();
            String newEmail = edtEmail.getText().toString().trim();

            if (newUsername.isEmpty() || newPassword.isEmpty() || newPhone.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            executorService.execute(() -> {
                boolean success = DatabaseHelper.updateUser(user.getUsername(), newUsername, newPassword, newPhone, newEmail);
                requireActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getContext(), "Sửa tài khoản thành công!", Toast.LENGTH_SHORT).show();
                        fetchUsers(); // Cập nhật danh sách tài khoản
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi sửa tài khoản!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
    private void deleteSelectedUsers() {
        if (selectedUsers.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn ít nhất một tài khoản để xóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Xác nhận xóa");
        builder.setMessage("Bạn có chắc chắn muốn xóa các tài khoản đã chọn?");
        builder.setPositiveButton("Xóa", (dialog, which) -> {
            executorService.execute(() -> {
                boolean success = DatabaseHelper.deleteUsers(selectedUsers);
                requireActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getContext(), "Xóa tài khoản thành công!", Toast.LENGTH_SHORT).show();
                        fetchUsers(); // Cập nhật danh sách
                    } else {
                        Toast.makeText(getContext(), "Lỗi khi xóa tài khoản!", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

}
