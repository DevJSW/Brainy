package com.brainy.erevu.Adapters;


/**
 * Created by Shephard on 8/7/2017.
 */

/*
public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{


    private ArrayList<Object> items;
    Context ctx;
    private final int VERTICAL = 1;
    private final int HORIZONTAL = 2;

    public MainAdapter(Context ctx, ArrayList<Object> items)
    {
        this.items = items;
        this.ctx = ctx;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view;
        RecyclerView.ViewHolder holder;
        switch (viewType) {
            case VERTICAL:
                view = inflater.inflate(R.layout.vertical, parent, false);
                holder = new VerticalViewHolder(view);
                break;
            case HORIZONTAL:
                view = inflater.inflate(R.layout.horizontal, parent, false);
                holder = new HorizontalViewHolder(view);
                break;

            default:
                view = inflater.inflate(R.layout.horizontal, parent, false);
                holder = new HorizontalViewHolder(view);
                break;
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VERTICAL)
            verticalView((VerticalViewHolder) holder);
        else if (holder.getItemViewType() == HORIZONTAL)
            horizontalView((HorizontalViewHolder) holder);

    }

  */
/*  private void verticalView(VerticalViewHolder holder) {

        QuestionAdapter adapter1 = new QuestionAdapter(getVerticalData());
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        holder.recyclerView.setAdapter(adapter1);
    }

    private void horizontalView(HorizontalViewHolder holder) {

        //AdsAdapter adapter = new AdsAdapter(getHorizontalData());
        QuestionAdapter adapter1 = new QuestionAdapter(getVerticalData());
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(ctx, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerView.setAdapter(adapter1);
    }*//*


    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof Question)
            return VERTICAL;
        if (items.get(position) instanceof Question)
            return HORIZONTAL;
        return -1;
    }

    public class HorizontalViewHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;

        HorizontalViewHolder(View itemView) {
            super(itemView);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.Ads_list);
        }

    }

    public class VerticalViewHolder extends RecyclerView.ViewHolder {

        RecyclerView recyclerView;

        VerticalViewHolder(View itemView) {
            super(itemView);
            recyclerView = (RecyclerView) itemView.findViewById(R.id.Questions_list);
        }

    }


}
*/

