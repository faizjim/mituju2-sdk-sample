package my.mimos.mitujusdk.search;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import my.mimos.mitujusdk.R;
import my.mimos.mituju.v2.ilpservice.db.TblPoints;

/**
 * Created by ariffin.ahmad on 16/06/2017.
 */

public class LocationListing extends RelativeLayout {
    private RecyclerView recycler_view;
    private TextView text_status;

    private LocationAdapter adapter          = new LocationAdapter();
    private List<TblPoints.ILPPoint> points  = new ArrayList<>();

    public ILocationListing callback;

    public interface ILocationListing {
        void onLocationSelected(TblPoints.ILPPoint point);
    }

    public LocationListing(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.location_listing, this, true);

        recycler_view = (RecyclerView) findViewById(R.id.listing_location_recyclerview);
        text_status   = (TextView) findViewById(R.id.listing_location_status);


        LinearLayoutManager layout_manager = new LinearLayoutManager(context);
        recycler_view.setHasFixedSize(true);
        recycler_view.setLayoutManager(layout_manager);
        recycler_view.setItemAnimator(new DefaultItemAnimator());
        recycler_view.setAdapter(adapter);
    }

    public void updatePoints(List<TblPoints.ILPPoint> points) {
        this.points = points;
        this.adapter.notifyDataSetChanged();
    }

    public void clear() {
        this.points.clear();
        this.adapter.notifyDataSetChanged();
    }

    private class LocationViewHolder extends RecyclerView.ViewHolder {
        public final CardLocationItem location_item;

        public LocationViewHolder(CardLocationItem location_item) {
            super(location_item);

            this.location_item = location_item;
            location_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (callback != null)
                        callback.onLocationSelected(points.get(getAdapterPosition()));
                }
            });
        }
    }


    private class LocationAdapter extends RecyclerView.Adapter<LocationViewHolder> {

        @Override
        public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new LocationViewHolder(new CardLocationItem(getContext(), null));
        }

        @Override
        public void onBindViewHolder(LocationViewHolder holder, int position) {
            if (position >= points.size())
                return;

            holder.location_item.update(points.get(position));
        }

        @Override
        public int getItemCount() {
            int count = points.size();

            if (count > 0)
                text_status.setText("");
            else
                text_status.setText("not found...");

            return count;
        }
    }
}
