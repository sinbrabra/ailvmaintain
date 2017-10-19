package com.maitianer.android.ailvmaintain.app.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.maitianer.android.ailvmaintain.R;

import java.util.List;
import java.util.Map;

/**
 * Created by czq on 2017/10/10.
 */

public class CarInformationAdapter extends BaseQuickAdapter<Map<String, Integer>, BaseViewHolder>{

    public CarInformationAdapter() {
        super(R.layout.item_error_information);
    }

    public CarInformationAdapter(List<Map<String, Integer>> data) {
        super(R.layout.item_error_information, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Map<String, Integer> item) {
        for(String key : item.keySet()){
            if(item.get(key) == 0){
                helper.setText(R.id.equipment, key);
                helper.setImageResource(R.id.iv_char, R.drawable.ic_tick);
            }
            else{
                helper.setText(R.id.equipment, key);
                helper.setImageResource(R.id.iv_char, R.drawable.ic_exclamatory);
            }
        }
    }
}
