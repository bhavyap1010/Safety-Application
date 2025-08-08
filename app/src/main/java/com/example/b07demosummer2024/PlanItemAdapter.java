package com.example.b07demosummer2024;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlanItemAdapter extends RecyclerView.Adapter<PlanItemAdapter.ViewHolder>{
    private List<PlanItem> items;

    public PlanItemAdapter(List<PlanItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public PlanItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_plan_item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanItemAdapter.ViewHolder holder, int position) {
        PlanItem item = items.get(position);

        holder.questionText.setText(item.getQuestion());

        StringBuilder sb = new StringBuilder();
        for (String ans : item.getAnswers()) {
            sb.append(ans).append("\n");
        }
        holder.answersText.setText(sb.toString().trim());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // this inner class holds references to views in a single row
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView questionText;
        TextView answersText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.questionText);
            answersText = itemView.findViewById(R.id.answersText);
        }
    }
}