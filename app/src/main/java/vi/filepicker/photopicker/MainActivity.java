package vi.filepicker.photopicker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.photopicker.OnPhotoPickListener;
import com.photopicker.PhotoPicker;
import com.photopicker.PhotoPreview;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuchangyou on 2016/12/6.
 */
public class MainActivity extends AppCompatActivity {
    private List<String> lists = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViewById(R.id.pick_picture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoPicker.init().setMaxCount(5).setShowCamera(true).startPick(MainActivity.this, new OnPhotoPickListener() {
                    @Override
                    public void onPhotoPick(boolean userCancel, List<String> list) {
                        if (userCancel) {
                            return;
                        }
                        lists.clear();
                        lists.addAll(list);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onPhotoCapture(String path) {
                        lists.add(path);
                        adapter.notifyDataSetChanged();
                    }
                });

            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);

    }

    private RecyclerView.Adapter adapter = new RecyclerView.Adapter() {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(MainActivity.this, R.layout.item, null);

            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            MyViewHolder myViewHolder = (MyViewHolder) holder;
            Glide.with(getApplicationContext()).load(new File(lists.get(position))).into(myViewHolder.imageView);
            myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PhotoPreview.init().setPhotoPaths(lists).setCurrentPos(position).setPreviewOnly(true).startPreview(MainActivity.this, null);
                }
            });
        }

        @Override
        public int getItemCount() {
            return lists.size();
        }
    };

    class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img);
        }
    }
}
