package com.example.appsmartstorage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.BaseAdapter;
import java.util.List;

public class UserAdapter extends BaseAdapter {
    private Context context;
    private List<UserAccount> userList;
    private AccountUserActivity fragment;
    private List<UserAccount> selectedUsers;

    public UserAdapter(AccountUserActivity fragment, List<UserAccount> userList, List<UserAccount> selectedUsers) {
        this.fragment = fragment;
        this.context = fragment.getContext();
        this.userList = userList;
        this.selectedUsers = selectedUsers;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        TextView txtAccount;
        TextView txtPassword;
        CheckBox checkboxAccount;
        Button btnEdit;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.account_item, parent, false);
            holder = new ViewHolder();
            holder.txtAccount = convertView.findViewById(R.id.txtAccount);
            holder.txtPassword = convertView.findViewById(R.id.txtPassword);
            holder.checkboxAccount = convertView.findViewById(R.id.checkboxAccount);
            holder.btnEdit = convertView.findViewById(R.id.btnEdit);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        UserAccount user = userList.get(position);
        holder.txtAccount.setText(user.getUsername());
        holder.txtPassword.setText(user.getPassword());

        // Đặt trạng thái checkbox dựa trên danh sách selectedUsers
        holder.checkboxAccount.setOnCheckedChangeListener(null); // Ngăn chặn sự kiện cũ
        holder.checkboxAccount.setChecked(selectedUsers.contains(user));

        // Khi checkbox thay đổi trạng thái
        holder.checkboxAccount.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedUsers.contains(user)) {
                    selectedUsers.add(user);
                }
            } else {
                selectedUsers.remove(user);
            }
        });

        // Xử lý khi nhấn nút "Sửa"
        holder.btnEdit.setOnClickListener(v -> fragment.showEditAccountDialog(user));

        return convertView;
    }
}
