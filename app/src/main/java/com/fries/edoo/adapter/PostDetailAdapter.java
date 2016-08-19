package com.fries.edoo.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;


import com.fries.edoo.activities.PostDetailActivity;
import com.fries.edoo.R;
import com.fries.edoo.helper.SQLiteHandler;
import com.fries.edoo.holder.AbstractHolder;
import com.fries.edoo.holder.ItemCommentDetailHolder;
import com.fries.edoo.holder.ItemPostDetailHolder;
import com.fries.edoo.models.ItemComment;
import com.fries.edoo.models.ItemTimeLine;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by TooNies1810 on 2/19/16.
 */
public class PostDetailAdapter extends RecyclerView.Adapter<AbstractHolder> {

    private Context mContext;
    private ItemTimeLine itemTimeline;
    private HashMap<String, String> user;

    public PostDetailAdapter(Context mContext, ItemTimeLine itemTimeline) {
        this.mContext = mContext;
        this.itemTimeline = itemTimeline;

        SQLiteHandler sqLite = new SQLiteHandler(mContext);
        user = sqLite.getUserDetails();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public AbstractHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_post_detail, parent, false);
            return new ItemPostDetailHolder(view, itemTimeline);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_comment_in_popup, parent, false);
            return new ItemCommentDetailHolder(view, itemTimeline, this);
        }
    }

    @Override
    public void onBindViewHolder(AbstractHolder holder, int position) {
        if (position != 0) {
            final ItemCommentDetailHolder commentHolder = (ItemCommentDetailHolder) holder;
            final ItemComment itemComment = itemTimeline.getItemComments().get(position - 1);
            commentHolder.setItemComment(itemComment);

            final CheckBox cbSolve = commentHolder.getCheckBoxVote();

            String authorId = itemTimeline.getIdAuthor();
            if (!user.get("uid").equalsIgnoreCase(authorId)) {
                cbSolve.setClickable(false);
                cbSolve.setVisibility(View.INVISIBLE);
            } else {
                cbSolve.setClickable(true);
                cbSolve.setVisibility(View.VISIBLE);

                cbSolve.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < itemTimeline.getItemComments().size(); i++) {
                            itemTimeline.getItemComments().get(i).setVote(false);
                        }
                        commentHolder.postSolve(itemComment.getIdComment());
                    }
                });
            }
        } else {
            ItemPostDetailHolder postDetailHolder = (ItemPostDetailHolder) holder;

            postDetailHolder.setTitle(itemTimeline.getTitle());
            postDetailHolder.setContentToWebview(itemTimeline.getContent());
            postDetailHolder.setAuthorName(itemTimeline.getName());
            postDetailHolder.setComment(itemTimeline.getItemComments().size() + "");
            postDetailHolder.setLike(itemTimeline.getLike() + "");
            if (itemTimeline.getLike() >= 0) {
                postDetailHolder.getIvLike().setImageResource(R.mipmap.ic_up_24);
            } else {
                postDetailHolder.getIvLike().setImageResource(R.mipmap.ic_down_24);
            }

            postDetailHolder.getTvCreateAt().setText(", " + itemTimeline.getCreateAt());

            postDetailHolder.setCbIsVote();
        }
    }

    @Override
    public int getItemCount() {
        return itemTimeline.getItemComments().size() + 1;
    }

    public void setItemTimeline(ItemTimeLine itemTimeline) {
        this.itemTimeline = itemTimeline;
        notifyDataSetChanged();
    }

    public void setItemComments(ArrayList<ItemComment> commentArr) {
        this.itemTimeline.setItemComments(commentArr);
        notifyDataSetChanged();
    }

    public void setSolveCmt(String cmtId) {
        ArrayList<ItemComment> cmts = itemTimeline.getItemComments();
        for (int i = 0; i < cmts.size(); i++) {
            if (cmts.get(i).getIdComment().equalsIgnoreCase(cmtId)) {
                cmts.get(i).setVote(true);
            } else {
                cmts.get(i).setVote(false);
            }
        }
        notifyDataSetChanged();
    }
}
