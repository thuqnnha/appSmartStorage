package com.example.appsmartstorage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        //
        Button btnLogin = findViewById(R.id.btnLogin);
        EditText edttAccount = findViewById(R.id.edttAccount);
        EditText edttPassWord = findViewById(R.id.edttPassWord);
//        edttAccount.setOnFocusChangeListener((v, hasFocus) -> {
//            if (hasFocus) {
//                edttAccount.setText("");
//            }
//        });
//
//        edttPassWord.setOnFocusChangeListener((v, hasFocus) -> {
//            if (hasFocus) {
//                edttPassWord.setText("");
//            }
//        });
        edttAccount.setText("admin");
        edttPassWord.setText("admin6789");
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edttAccount.getText().toString().trim();
                String password = edttPassWord.getText().toString().trim();
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                    return;
                }
                new Thread(() -> {
                    try {
                        Connection conn = DatabaseHelper.connect();
                        if (conn != null) {
                            String query = "SELECT IDRole FROM quanlykho_users WHERE UserName = ? AND PassWord = ?";
                            PreparedStatement stmt = conn.prepareStatement(query);
                            stmt.setString(1, username);
                            stmt.setString(2, password);
                            ResultSet rs = stmt.executeQuery();

                            if (rs.next()) {
                                int role = rs.getInt("IDRole");
                                if (role == 1) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this, "Đăng nhập tài khoản admin!", Toast.LENGTH_SHORT).show();
                                        //Load admin form
                                        Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                                        intent.putExtra("role", role); // truyền IDRole
                                        startActivity(intent);
                                    });
                                } else if (role == 2) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this, "Đăng nhập tài khoản user!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                                        intent.putExtra("role", role); // truyền IDRole
                                        startActivity(intent);
                                    });
                                } else {
                                    runOnUiThread(() -> {
                                        Toast.makeText(MainActivity.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } else{
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                                });
                            }
                            rs.close();
                            stmt.close();
                            conn.close();
                        } else {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show());
                        }
                    } catch (Exception e) {
                        Log.e("Database", "Lỗi truy vấn MySQL: " + e.getMessage(), e);
                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Lỗi hệ thống!", Toast.LENGTH_SHORT).show());
                    }
                }).start();
            }

        });
    }
}