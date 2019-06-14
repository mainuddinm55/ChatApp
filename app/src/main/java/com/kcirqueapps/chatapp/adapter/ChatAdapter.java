package com.kcirqueapps.chatapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.Conversion;
import com.kcirqueapps.chatapp.network.model.User;
import com.kcirqueapps.chatapp.utils.PrefUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends PagedListAdapter<Conversion, ChatAdapter.MessageHolder> {
    private static final int MESSAGE_TYPE_LEFT = 1;
    private static final int MESSAGE_TYPE_RIGHT = 2;
    private User currentUser;
    private ItemClickedListener itemClickedListener;
    private String url;

    private static DiffUtil.ItemCallback<Conversion> DIFF_UTIL = new DiffUtil.ItemCallback<Conversion>() {
        @Override
        public boolean areItemsTheSame(@NonNull Conversion oldItem, @NonNull Conversion newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Conversion oldItem, @NonNull Conversion newItem) {
            return oldItem.getId() == newItem.getId();
        }
    };

    public ChatAdapter(Context context) {
        super(DIFF_UTIL);
        currentUser = new PrefUtils(context).getUser();
    }


    public void setItemClickedListener(ItemClickedListener itemClickedListener) {
        this.itemClickedListener = itemClickedListener;
    }


    @NonNull
    @Override
    public MessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = null;
        if (viewType == MESSAGE_TYPE_LEFT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_left, parent, false);
        } else if (viewType == MESSAGE_TYPE_RIGHT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_right, parent, false);
        }
        assert view != null;
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageHolder holder, int position) {
        Conversion conversion = getItem(position);
        holder.bindTo(conversion);
    }

    @Override
    public int getItemViewType(int position) {
        if (currentUser != null) {
            if (getItem(position).getSenderId() == currentUser.getId()) {
                return MESSAGE_TYPE_RIGHT;
            } else {
                return MESSAGE_TYPE_LEFT;
            }
        }
        return -1;
    }

    class MessageHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImageView;
        TextView messageTextView, msgSeenTextView, fileNameTextView;
        ImageView picImageView;
        ImageButton fileDownloadBtn;

        MessageHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            messageTextView = itemView.findViewById(R.id.show_message);
            msgSeenTextView = itemView.findViewById(R.id.msg_seen_text_view);
            picImageView = itemView.findViewById(R.id.show_pic);
            fileDownloadBtn = itemView.findViewById(R.id.download_btn);
            fileNameTextView = itemView.findViewById(R.id.file_name_text_view);

        }

        void bindTo(Conversion conversion) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) msgSeenTextView.getLayoutParams();
            if (conversion.getMediaUrl() != null) {
                messageTextView.setVisibility(conversion.getMessage() == null || conversion.getMessage().equals("") ? View.GONE : View.VISIBLE);
                messageTextView.setText(conversion.getMessage());
                if (conversion.getMediaType().contains("image")) {
                    picImageView.setVisibility(View.VISIBLE);
                    fileDownloadBtn.setVisibility(View.GONE);
                    fileNameTextView.setVisibility(View.GONE);
                    String url = ApiClient.URL + conversion.getMediaUrl();
                    //Picasso.with(itemView.getContext()).load(url).into(picImageView);
                    Glide.with(itemView.getContext()).load(url).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL))
                            .placeholder(R.drawable.placeholder).into(picImageView);
                } else if (conversion.getMediaType().contains("application")) {
                    final String url = ApiClient.URL + conversion.getMediaUrl();
                    fileNameTextView.setText(conversion.getFileName());
                    fileDownloadBtn.setVisibility(View.VISIBLE);
                    fileNameTextView.setVisibility(View.VISIBLE);
                    fileDownloadBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (itemClickedListener != null) {
                                itemClickedListener.onDownloadItemClicked(url);
                            }
                        }
                    });
                } else {
                    fileDownloadBtn.setVisibility(View.GONE);
                    fileNameTextView.setVisibility(View.GONE);
                }

                layoutParams.addRule(RelativeLayout.BELOW, messageTextView.getId());
            } else {
                fileDownloadBtn.setVisibility(View.GONE);
                fileNameTextView.setVisibility(View.GONE);
                picImageView.setVisibility(View.GONE);
                messageTextView.setVisibility(View.VISIBLE);
                messageTextView.setText(conversion.getMessage());
                layoutParams.addRule(RelativeLayout.BELOW, messageTextView.getId());
            }
            msgSeenTextView.setLayoutParams(layoutParams);
            Glide.with(itemView.getContext()).load(url).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).placeholder(R.drawable.profile_user).error(R.drawable.profile_user).into(profileImageView);

            if (getAdapterPosition() == (getCurrentList().size() - 1)) {
                if (getItem(getAdapterPosition()).getReceiverSeen() > 0) {
                    msgSeenTextView.setText("seen");
                } else {
                    msgSeenTextView.setText("delivered");
                }
            } else {
                msgSeenTextView.setVisibility(View.GONE);
            }
        }
    }

    public interface ItemClickedListener {
        void onDownloadItemClicked(String url);
    }
}
