package com.example.firestockbilly;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private static final String TAG = "BillyDebug - UserAdapter";

    private List<User> userList;
    private boolean isAdmin;
    private final RemoveUserClickListener removeUserClickListener;
    private String currentUserId;

    public UserAdapter(List<User> userList, boolean isAdmin, RemoveUserClickListener listener) {
        this.userList = userList;
        this.isAdmin = isAdmin;
        this.removeUserClickListener = listener;
        this.currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        Log.d(TAG, "User displayName: " + user.getName());
        holder.userNameTextView.setText(user.isAdmin() ? user.getName() + " [Admin]" : user.getName());



        if (isAdmin && !user.getId().equals(currentUserId)) {
            holder.removeUserButton.setVisibility(View.VISIBLE);
            holder.removeUserButton.setOnClickListener(v -> removeUserClickListener.onRemoveUserClick(user.getId()));
        } else {
            holder.removeUserButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;
        Button removeUserButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            removeUserButton = itemView.findViewById(R.id.removeUserButton);
        }
    }

    public interface RemoveUserClickListener {
        void onRemoveUserClick(String userId);
    }
}
