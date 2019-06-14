package com.kcirqueapps.chatapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.internal.bind.util.ISO8601Utils;
import com.kcirqueapps.chatapp.R;
import com.kcirqueapps.chatapp.network.api.Api;
import com.kcirqueapps.chatapp.network.api.ApiClient;
import com.kcirqueapps.chatapp.network.model.Chat;
import com.kcirqueapps.chatapp.network.model.Conversion;
import com.kcirqueapps.chatapp.network.model.Group;
import com.kcirqueapps.chatapp.network.model.HttpResponse;
import com.kcirqueapps.chatapp.network.model.User;
import com.kcirqueapps.chatapp.utils.CircleTransform;
import com.kcirqueapps.chatapp.utils.PrefUtils;
import com.squareup.picasso.Picasso;


import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.FriendHolder> {
    private List<Chat> chatList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private User currentUser;
    private Api api;

    public ChatListAdapter(Context context) {
        currentUser = new PrefUtils(context).getUser();
        api = ApiClient.getInstance().getApi();
    }

    @NonNull
    @Override
    public FriendHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.conversion_row_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListAdapter.FriendHolder holder, int position) {
        final Chat chat = chatList.get(position);
        holder.bindTo(chat);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void setChatList(List<Chat> chatList) {
        this.chatList = chatList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    class FriendHolder extends RecyclerView.ViewHolder {
        final CircleImageView profileImageView, isSeenImageView;
        final TextView nameTextView, messageTextView, dateTextView;
        final RelativeLayout rootView;

        FriendHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.root_view);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
            isSeenImageView = itemView.findViewById(R.id.is_seen_image_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            messageTextView = itemView.findViewById(R.id.message_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);

        }

        void bindTo(final Chat chat) {

            if (chat.getReceiverId() != 0 && chat.getConversionType().equals("Single")) {
                api.getSingleConversions(currentUser.getId(), chat.getReceiverId(), 2, 1, 2)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<HttpResponse<List<Conversion>>>() {
                            @Override
                            public void onSuccess(HttpResponse<List<Conversion>> listHttpResponse) {
                                if (!listHttpResponse.isError()) {
                                    List<Conversion> conversionList = listHttpResponse.getResponse();

                                    final Conversion conversion = conversionList.get(0);
                                    dateTextView.setText(formatDate(conversion.getSendTime()));
                                    if (chat.getReceiverId() == conversion.getReceiverId()) {
                                        if (conversion.getMediaType().contains("image")) {
                                            messageTextView.setText("You: Image");
                                        } else if (conversion.getMediaType().contains("application")) {
                                            messageTextView.setText("You: Attachment");
                                        } else {
                                            messageTextView.setText(String.format("You: %s", conversion.getMessage()));
                                        }
                                    } else {
                                        isSeenImageView.setVisibility(View.INVISIBLE);
                                        messageTextView.setText(conversion.getMessage());
                                    }
                                    if (conversion.getReceiverId() == currentUser.getId() && conversion.getReceiverSeen() == 0) {
                                        messageTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.un_seen_color));
                                        messageTextView.setTypeface(null, Typeface.BOLD);
                                    }else {
                                        messageTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.seen_color));
                                        messageTextView.setTypeface(null, Typeface.NORMAL);
                                    }

                                    api.getUser(chat.getReceiverId()).subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new DisposableSingleObserver<HttpResponse<User>>() {
                                                @Override
                                                public void onSuccess(final HttpResponse<User> userHttpResponse) {
                                                    if (!userHttpResponse.isError()) {
                                                        final String url = ApiClient.URL + userHttpResponse.getResponse().getPhotoUrl();
                                                        nameTextView.setText(String.format("%s %s", userHttpResponse.getResponse().getFirstName(), userHttpResponse.getResponse().getLastName()));
                                                        Glide.with(itemView).load(url).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).placeholder(R.drawable.profile_user).error(R.drawable.profile_user).into(profileImageView);
                                                        if (chat.getReceiverId() == conversion.getReceiverId() && conversion.getReceiverSeen() > 0) {
                                                            Glide.with(itemView).load(url).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).placeholder(R.drawable.profile_user).error(R.drawable.profile_user).into(isSeenImageView);

                                                        } else if (chat.getReceiverId() == conversion.getReceiverId()) {
                                                            Glide.with(itemView).load(R.drawable.ic_double_check).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL)).placeholder(R.drawable.ic_double_check).error(R.drawable.ic_double_check).into(isSeenImageView);
                                                        } else {
                                                            isSeenImageView.setVisibility(View.INVISIBLE);
                                                            messageTextView.setText(conversion.getMessage());
                                                        }
                                                        rootView.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                if (onItemClickListener != null) {
                                                                    onItemClickListener.onUserClicked(userHttpResponse.getResponse(), conversion);
                                                                }
                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    e.printStackTrace();
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }
                        });


            } else if (chat.getReceiverId() != 0 && chat.getConversionType().equals("Group")) {

                api.getGroupConversions(chat.getReceiverId(), 2, 1, 1)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new DisposableSingleObserver<HttpResponse<List<Conversion>>>() {

                            @Override
                            public void onSuccess(HttpResponse<List<Conversion>> listHttpResponse) {
                                if (!listHttpResponse.isError()) {
                                    List<Conversion> chatList = listHttpResponse.getResponse();
                                    final Conversion conversion = chatList.get(chatList.size() - 1);
                                    dateTextView.setText(formatDate(conversion.getSendTime()));
                                    if (chat.getReceiverId() == conversion.getReceiverId() && conversion.getReceiverSeen() > 0) {
                                        messageTextView.setText(String.format("You: %s", conversion.getMessage()));
                                        Glide.with(itemView.getContext()).load(R.drawable.profile_user).into(isSeenImageView);
                                    } else if (chat.getReceiverId() == conversion.getReceiverId()) {
                                        Glide.with(itemView.getContext()).load(R.drawable.ic_double_check).into(isSeenImageView);
                                        messageTextView.setText(String.format("You: %s", conversion.getMessage()));
                                    } else {
                                        isSeenImageView.setVisibility(View.INVISIBLE);
                                        messageTextView.setText(conversion.getMessage());
                                    }
                                    api.getGroup(chat.getReceiverId()).subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(new DisposableSingleObserver<HttpResponse<Group>>() {
                                                @Override
                                                public void onSuccess(final HttpResponse<Group> groupHttpResponse) {
                                                    if (!groupHttpResponse.isError()) {
                                                        nameTextView.setText(groupHttpResponse.getResponse().getName());
                                                        rootView.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {
                                                                if (onItemClickListener != null) {
                                                                    onItemClickListener.onGroupClicked(groupHttpResponse.getResponse(), conversion);
                                                                }
                                                            }
                                                        });
                                                    }
                                                }

                                                @Override
                                                public void onError(Throwable e) {
                                                    e.printStackTrace();
                                                }
                                            });
                                } else {
                                    Log.e("", "onSuccess: " + listHttpResponse.getMessage());
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                            }
                        });

            } else {
                rootView.setVisibility(View.GONE);
            }


        }

        String formatDate(String stringDate) {
            Calendar currentCalender = Calendar.getInstance();
            try {
                Date date = ISO8601Utils.parse(stringDate, new ParsePosition(0));
                Calendar specifiedCalender = Calendar.getInstance();
                specifiedCalender.setTime(date);
                currentCalender.setFirstDayOfWeek(Calendar.SATURDAY);
                specifiedCalender.setFirstDayOfWeek(Calendar.SATURDAY);

                boolean isToday = currentCalender.get(Calendar.YEAR) == specifiedCalender.get(Calendar.YEAR)
                        && currentCalender.get(Calendar.MONTH) == specifiedCalender.get(Calendar.MONTH)
                        && currentCalender.get(Calendar.DAY_OF_MONTH) == specifiedCalender.get(Calendar.DAY_OF_MONTH);
                boolean isLastWeek = specifiedCalender.get(Calendar.WEEK_OF_MONTH) ==
                        (currentCalender.get(Calendar.WEEK_OF_MONTH));

                if (isToday) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
                    return simpleDateFormat.format(date);
                } else if (isLastWeek) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
                    return simpleDateFormat.format(date);
                } else {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM, dd", Locale.ENGLISH);
                    return simpleDateFormat.format(date);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public interface OnItemClickListener {
        void onUserClicked(User user, Conversion conversion);

        void onGroupClicked(Group group, Conversion conversion);
    }
}
