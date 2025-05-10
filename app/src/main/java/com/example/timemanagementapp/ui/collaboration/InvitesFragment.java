package com.example.timemanagementapp.ui.collaboration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.timemanagementapp.R;
import com.example.timemanagementapp.data.local.entity.CollaborationInvite;
import com.example.timemanagementapp.data.local.entity.CurrentUserManager;
import com.example.timemanagementapp.data.local.entity.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InvitesFragment extends Fragment {

    private RecyclerView recyclerViewInvites;
    private TextView textViewEmpty;
    private CollaborationViewModel viewModel;
    private InvitesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invites, container, false);

        recyclerViewInvites = view.findViewById(R.id.recycler_view_invites);
        textViewEmpty = view.findViewById(R.id.text_view_empty);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(CollaborationViewModel.class);

        recyclerViewInvites.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new InvitesAdapter();
        recyclerViewInvites.setAdapter(adapter);

        // Загружаем приглашения для текущего пользователя
        User currentUser = CurrentUserManager.getCurrentUser();
        if (currentUser != null) {
            viewModel.loadPendingInvitesForUser(currentUser.getEmail());

            viewModel.getPendingInvites().observe(getViewLifecycleOwner(), invites -> {
                if (invites != null && !invites.isEmpty()) {
                    adapter.setInvites(invites);
                    textViewEmpty.setVisibility(View.GONE);
                    recyclerViewInvites.setVisibility(View.VISIBLE);
                } else {
                    textViewEmpty.setVisibility(View.VISIBLE);
                    recyclerViewInvites.setVisibility(View.GONE);
                }
            });
        }

        viewModel.getInviteStatus().observe(getViewLifecycleOwner(), status -> {
            if (status != null && !status.isEmpty()) {
                Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class InvitesAdapter extends RecyclerView.Adapter<InvitesAdapter.InviteViewHolder> {
        private List<CollaborationInvite> invites = new ArrayList<>();

        class InviteViewHolder extends RecyclerView.ViewHolder {
            TextView textViewProjectName;
            TextView textViewInviter;
            TextView textViewDate;
            TextView textViewRole;
            Button buttonAccept;
            Button buttonReject;

            InviteViewHolder(View itemView) {
                super(itemView);
                textViewProjectName = itemView.findViewById(R.id.text_view_project_name);
                textViewInviter = itemView.findViewById(R.id.text_view_inviter);
                textViewDate = itemView.findViewById(R.id.text_view_date);
                textViewRole = itemView.findViewById(R.id.text_view_role);
                buttonAccept = itemView.findViewById(R.id.button_accept);
                buttonReject = itemView.findViewById(R.id.button_reject);
            }
        }

        @NonNull
        @Override
        public InviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_invite, parent, false);
            return new InviteViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InviteViewHolder holder, int position) {
            CollaborationInvite invite = invites.get(position);
            
            // Загружаем данные о проекте и пользователе асинхронно
            viewModel.getProjectName(invite.getProjectId()).observe(getViewLifecycleOwner(), projectName -> {
                if (projectName != null) {
                    holder.textViewProjectName.setText("Проект: " + projectName);
                }
            });

            viewModel.getUserName(invite.getInviterUserId()).observe(getViewLifecycleOwner(), userName -> {
                if (userName != null) {
                    holder.textViewInviter.setText("Від: " + userName);
                }
            });

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());
            holder.textViewDate.setText("Дата: " + dateFormat.format(invite.getCreatedAt()));
            
            String role = "Редактор";
            if ("viewer".equals(invite.getRole())) {
                role = "Переглядач";
            }
            holder.textViewRole.setText("Роль: " + role);

            holder.buttonAccept.setOnClickListener(v -> {
                viewModel.respondToInvite(invite.getInviteId(), true);
            });

            holder.buttonReject.setOnClickListener(v -> {
                viewModel.respondToInvite(invite.getInviteId(), false);
            });
        }

        @Override
        public int getItemCount() {
            return invites.size();
        }

        void setInvites(List<CollaborationInvite> invites) {
            this.invites = invites;
            notifyDataSetChanged();
        }
    }
} 