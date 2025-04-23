package com.example.appsmartstorage;

public class Product {
    private String maKho;
    private String maVatTu;
    private String tenVatTu;
    private String donViTinh;
    private String viTri;
    private int soLuongNhap;
    private String thoiGianNhap;
    private double giaTien;
    private int soLuong; // Số lượng thêm vào giỏ hàng
    // Constructor
    public Product(String maKho, String maVatTu, String tenVatTu, String donViTinh,
                   String viTri, int soLuongNhap, String thoiGianNhap, double giaTien) {
        this.maKho = maKho;
        this.maVatTu = maVatTu;
        this.tenVatTu = tenVatTu;
        this.donViTinh = donViTinh;
        this.viTri = viTri;
        this.soLuongNhap = soLuongNhap;
        this.thoiGianNhap = thoiGianNhap;
        this.giaTien = giaTien;
        this.soLuong = 1; // mặc định là 1 khi mới thêm vào
    }

    // Getter và Setter
    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }
    public void increaseSoLuong() {
        this.soLuong++;
    }
    public String getMaKho() {
        return maKho;
    }

    public void setMaKho(String maKho) {
        this.maKho = maKho;
    }

    public String getMaVatTu() {
        return maVatTu;
    }

    public void setMaVatTu(String maVatTu) {
        this.maVatTu = maVatTu;
    }

    public String getTenVatTu() {
        return tenVatTu;
    }

    public void setTenVatTu(String tenVatTu) {
        this.tenVatTu = tenVatTu;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public String getViTri() {
        return viTri;
    }

    public void setViTri(String viTri) {
        this.viTri = viTri;
    }

    public int getSoLuongNhap() {
        return soLuongNhap;
    }

    public void setSoLuongNhap(int soLuongNhap) {
        this.soLuongNhap = soLuongNhap;
    }

    public String getThoiGianNhap() {
        return thoiGianNhap;
    }

    public void setThoiGianNhap(String thoiGianNhap) {
        this.thoiGianNhap = thoiGianNhap;
    }

    public double getGiaTien() {
        return giaTien;
    }

    public void setGiaTien(double giaTien) {
        this.giaTien = giaTien;
    }
}
