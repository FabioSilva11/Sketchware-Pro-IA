package pro.sketchware.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.material.chip.Chip;

import java.util.List;

import pro.sketchware.R;

public class SkillChipAdapter extends BaseAdapter {
    private Context context;
    private List<String> skills;
    private LayoutInflater inflater;

    public SkillChipAdapter(Context context, List<String> skills) {
        this.context = context;
        this.skills = skills;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return skills.size();
    }

    @Override
    public Object getItem(int position) {
        return skills.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_skill_chip, parent, false);
            holder = new ViewHolder();
            holder.chip = convertView.findViewById(R.id.skill_chip);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        String skill = skills.get(position);
        holder.chip.setText(skill);
        
        return convertView;
    }
    
    static class ViewHolder {
        Chip chip;
    }
}
