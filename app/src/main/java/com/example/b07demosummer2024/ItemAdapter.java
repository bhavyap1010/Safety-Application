package com.example.b07demosummer2024;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    private List<Item> itemList;
    private String currentCategory;
    public ItemAdapter(List<Item> itemList) {

        this.itemList = itemList;
        this.currentCategory = "";
    }

    // Method to update the category
    public void setCurrentCategory(String category) {
        this.currentCategory = category != null ? category.toLowerCase() : "";
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_item_adapater, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.textViewTitle.setText(item.getTitle());
        holder.textViewDescription.setText("Description " + item.getDescription());
        holder.textViewDate.setText("Date: " + item.getDate());
        holder.textViewGovId.setText("ID: " + item.getGovId());
        holder.textViewCourtOrders.setText("CourtOrders: " + item.getCourtOrder());
        holder.textViewName.setText("Name: " + item.getName());
        holder.textViewRelationship.setText("Relationship: " + item.getRelationship());
        holder.textViewPhone.setText("Phone: " + item.getPhone());
        holder.textViewAddress.setText("Address: " + item.getAddress());
        holder.textViewNote.setText("Note: " + item.getNotes());
        holder.textViewMedName.setText("Medication Name: " + item.getMedName());
        holder.textViewDosage.setText("Dosage: " + item.getDosage());

        holder.itemOptionsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 'v' is the clicked ImageView
                PopupMenu popup = new PopupMenu(v.getContext(), v); // Use v.getContext()
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        int itemId = menuItem.getItemId();
                        //Bring to edit page
                        // You have access to 'item' (the data for this row)
                        // and 'position' (the adapter position of this row)
                        if (itemId == R.id.menu_edit) {
                            Toast.makeText(v.getContext(), "Edit: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                            // TODO: Implement edit functionality for 'item'
                            return true;
                        } else if (itemId == R.id.menu_delete) {
                            Toast.makeText(v.getContext(), "Delete: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                            // TODO: Implement delete functionality
                            //Just bring to the delete page
                            // Example:
                            // if (position != RecyclerView.NO_POSITION) {
                            //     itemList.remove(position);
                            //     notifyItemRemoved(position);
                            //     notifyItemRangeChanged(position, itemList.size());
                            // }

                            return true;
                        } else {
                            return false;
                        }
                    }
                });
                popup.show();
            }
        });

        updateVisibilityBasedOnCategory(holder, item, currentCategory);
    }

    private void updateVisibilityBasedOnCategory(@NonNull ItemViewHolder holder, Item item, String category) {
        switch (category){
            case "document":
                holder.textViewGovId.setVisibility(View.VISIBLE);
                holder.textViewCourtOrders.setVisibility(View.VISIBLE);

                holder.textViewName.setVisibility(View.GONE);
                holder.textViewRelationship.setVisibility(View.GONE);
                holder.textViewPhone.setVisibility(View.GONE);

                holder.textViewAddress.setVisibility(View.GONE);
                holder.textViewNote.setVisibility(View.GONE);

                holder.textViewMedName.setVisibility(View.GONE);
                holder.textViewDosage.setVisibility(View.GONE);

                break;
            case "emergency contact":
                holder.textViewGovId.setVisibility(View.GONE);
                holder.textViewCourtOrders.setVisibility(View.GONE);

                holder.textViewName.setVisibility(View.VISIBLE);
                holder.textViewRelationship.setVisibility(View.VISIBLE);
                holder.textViewPhone.setVisibility(View.VISIBLE);

                holder.textViewAddress.setVisibility(View.GONE);
                holder.textViewNote.setVisibility(View.GONE);

                holder.textViewMedName.setVisibility(View.GONE);
                holder.textViewDosage.setVisibility(View.GONE);

                break;
            case "safe location":
                holder.textViewGovId.setVisibility(View.GONE);
                holder.textViewCourtOrders.setVisibility(View.GONE);

                holder.textViewName.setVisibility(View.GONE);
                holder.textViewRelationship.setVisibility(View.GONE);
                holder.textViewPhone.setVisibility(View.GONE);

                holder.textViewAddress.setVisibility(View.VISIBLE);
                holder.textViewNote.setVisibility(View.VISIBLE);

                holder.textViewMedName.setVisibility(View.GONE);
                holder.textViewDosage.setVisibility(View.GONE);

                break;
            case "medication":
                holder.textViewGovId.setVisibility(View.GONE);
                holder.textViewCourtOrders.setVisibility(View.GONE);

                holder.textViewName.setVisibility(View.GONE);
                holder.textViewRelationship.setVisibility(View.GONE);
                holder.textViewPhone.setVisibility(View.GONE);

                holder.textViewAddress.setVisibility(View.GONE);
                holder.textViewNote.setVisibility(View.GONE);

                holder.textViewMedName.setVisibility(View.VISIBLE);
                holder.textViewDosage.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewDescription, textViewDate, textViewGovId, textViewCourtOrders, textViewName, textViewRelationship, textViewPhone, textViewAddress, textViewNote, textViewMedName, textViewDosage;
        ImageView itemOptionsImageView;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewGovId = itemView.findViewById(R.id.textViewGovId);
            textViewCourtOrders = itemView.findViewById(R.id.textViewCourtOrders);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewRelationship = itemView.findViewById(R.id.textViewRelationship);
            textViewPhone = itemView.findViewById(R.id.textViewPhone);
            textViewAddress = itemView.findViewById(R.id.textViewAddress);
            textViewNote = itemView.findViewById(R.id.textViewNote);
            textViewMedName = itemView.findViewById(R.id.textViewMedName);
            textViewDosage = itemView.findViewById(R.id.textViewDosage);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            itemOptionsImageView = itemView.findViewById(R.id.imageView);
        }
    }
}
