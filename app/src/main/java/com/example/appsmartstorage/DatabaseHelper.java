package com.example.appsmartstorage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.sql.Timestamp;

public class DatabaseHelper  {
    private static final String URL = "jdbc:mysql://pvl.vn:3306/admin_db";
    private static final String USER = "raspberry";
    private static final String PASSWORD = "admin6789@";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DatabaseHelper(Context context) {
    }

    public void close() {
        executorService.shutdown();
    }
    // Kết nối đến cơ sở dữ liệu
    public static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

    // Lấy dữ liệu từ bảng quanlykho_data
    public static List<String[]> getDataFromDatabase() {
        List<String[]> data = new ArrayList<>();
        try (Connection connection = connect()) {
            if (connection != null) {
                String query = "SELECT MaKho, MaVatTu, TenVatTu, DonViTinh, ViTri, SoLuongNhap, ThoiGianNhap, GiaTien FROM quanlykho_data";
                try (PreparedStatement ps = connection.prepareStatement(query);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        data.add(new String[]{
                                rs.getString("MaKho"),
                                rs.getString("MaVatTu"),
                                rs.getString("TenVatTu"),
                                rs.getString("DonViTinh"),
                                rs.getString("ViTri"),
                                rs.getString("SoLuongNhap"),
                                rs.getString("ThoiGianNhap"),
                                rs.getString("GiaTien")
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
    // Lấy danh sách tài khoản từ MySQL
    public static List<UserAccount> getUserAccounts() {
        List<UserAccount> userList = new ArrayList<>();
        try (Connection connection = connect()) {
            if (connection != null) {
                String query = "SELECT UserName, PassWord, Phone, Email, IDRole FROM quanlykho_users"; // Thêm cột ID
                try (PreparedStatement ps = connection.prepareStatement(query);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {
                        userList.add(new UserAccount(
                                rs.getString("UserName"),
                                rs.getString("PassWord"),
                                rs.getString("Phone"),
                                rs.getString("Email"),
                                rs.getInt("IDRole")
                        ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }

    public static boolean insertUser(String username, String password, String phone, String email) {
        String query = "INSERT INTO quanlykho_users (Username, Password, Phone, Email, IdRole) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, phone);
            ps.setString(4, email);
            ps.setInt(5, 2); // Mặc định IdRole = 2

            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean updateUser(String oldusername, String newusername, String password, String phone, String email) {
        String query = "INSERT INTO quanlykho_users (Username = ?, Password = ?, Phone = ?, Email = ? WHERE Username = ?)";
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, newusername);
            ps.setString(2, password);
            ps.setString(3, phone);
            ps.setString(4, email);
            ps.setString(5, oldusername);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean deleteUsers(List<UserAccount> users) {
        if (users.isEmpty()) return false;

        String query = "DELETE FROM quanlykho_users WHERE Username = ?";
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            for (UserAccount user : users) {
                ps.setString(1, user.getUsername());
                ps.addBatch(); // Thêm vào batch để xử lý nhiều tài khoản cùng lúc
            }
            int[] rowsDeleted = ps.executeBatch();
            return rowsDeleted.length > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateData(String maKho, String maVatTu, String tenVatTu, String donViTinh,
                                     String viTri, String soLuongNhap, String giaTien) {
        String query = "INSERT INTO quanlykho_data (MaKho, MaVatTu, TenVatTu, DonViTinh, ViTri, SoLuongNhap, ThoiGianNhap, GiaTien) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, maKho);
            ps.setString(2, maVatTu);
            ps.setString(3, tenVatTu);
            ps.setString(4, donViTinh);
            ps.setString(5, viTri);
            ps.setString(6, soLuongNhap);

            // Thời gian hiện tại
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis())); // Thời gian hiện tại
            ps.setString(8, giaTien);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static Product getProduct(String maVatTu) {
        try (Connection connection = connect()) {
            if (connection != null) {
                String query = "SELECT MaKho, MaVatTu, TenVatTu, DonViTinh, ViTri, SoLuongNhap, ThoiGianNhap, GiaTien FROM quanlykho_data WHERE MaVatTu = ?";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setString(1, maVatTu);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        return new Product(
                                rs.getString("MaKho"),
                                rs.getString("MaVatTu"),
                                rs.getString("TenVatTu"),
                                rs.getString("DonViTinh"),
                                rs.getString("ViTri"),
                                rs.getInt("SoLuongNhap"),
                                rs.getString("ThoiGianNhap"),
                                rs.getDouble("GiaTien")
                        );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static boolean updateCount(int soLuongBan, String maVatTu) {
        String query = "UPDATE quanlykho_data SET SoLuongNhap = SoLuongNhap - ? WHERE MaVatTu = ? AND SoLuongNhap >= ?";

        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, soLuongBan);
            ps.setString(2, maVatTu);
            ps.setInt(3, soLuongBan);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static List<String> checkAndUpdateStock(List<Product> productList) {
        List<String> errorProducts = new ArrayList<>();

        try (Connection connection = connect()) {
            connection.setAutoCommit(false); // Bắt đầu transaction

            for (Product product : productList) {
                String maVatTu = product.getMaVatTu();
                int soLuongBan = product.getSoLuong();

                // Lấy số lượng hiện tại
                String selectQuery = "SELECT SoLuongNhap FROM quanlykho_data WHERE MaVatTu = ?";
                try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
                    selectStmt.setString(1, maVatTu);
                    ResultSet rs = selectStmt.executeQuery();

                    if (rs.next()) {
                        int currentStock = rs.getInt("SoLuongNhap");

                        if (currentStock >= soLuongBan) {
                            // Đủ hàng → cập nhật số lượng
                            String updateQuery = "UPDATE quanlykho_data SET SoLuongNhap = SoLuongNhap - ? WHERE MaVatTu = ?";
                            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                                updateStmt.setInt(1, soLuongBan);
                                updateStmt.setString(2, maVatTu);
                                updateStmt.executeUpdate();
                            }
                        } else {
                            // Không đủ hàng
                            errorProducts.add(maVatTu);
                        }
                    } else {
                        // Không tìm thấy sản phẩm
                        errorProducts.add(maVatTu);
                    }
                }
            }

            if (errorProducts.isEmpty()) {
                connection.commit(); // Xác nhận nếu không có lỗi
            } else {
                connection.rollback(); // Có lỗi → hủy hết
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorProducts.add("LỖI HỆ THỐNG");
        }

        return errorProducts;
    }




}