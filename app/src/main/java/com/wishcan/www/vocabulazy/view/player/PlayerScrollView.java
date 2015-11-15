package com.wishcan.www.vocabulazy.view.player;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.wishcan.www.vocabulazy.R;
import com.wishcan.www.vocabulazy.view.adapter.LinkedListPagerAdapter;

/**
 * Created by swallow on 2015/8/13.
 *
 * Here is how our PlayerView constructed.
 * PlayerView is a ScrollView, including a LinearLayout child, which is composed with
 * some rectangle views, such as TextViews.
 *
 */
public class PlayerScrollView extends RelativeLayout {

    public interface OnPlayerScrollStoppedListener{
        void onPlayerScrollStopped();
    }

    public interface OnFocusChangedListener {
        void onFocusChanged(int position);
    }

    public interface OnItemPreparedListener{
        void onInitialItemPrepared();
        void onFinalItemPrepared();
    }

    private OnPlayerScrollStoppedListener mOnPlayerScrollStoppedListener;

    private OnFocusChangedListener mOnFocusChangedListener;

    private OnItemPreparedListener mOnItemPreparedListener;

    public static final int DEFAULT_PLAYER_LIST_ITEM_VIEW_RES_ID = R.layout.player_layout;

    public static final int DEFAULT_PLAYER_LIST_ITEM0_RES_ID = R.id.player_voc_spell;

    public static final int DEFAULT_PLAYER_LIST_ITEM1_RES_ID = R.id.player_voc_translation;

    public static final int DEFAULT_PLAYER_DETAIL_ITEM0_RES_ID = R.id.player_voc_spell_detail;

    public static final int DEFAULT_PLAYER_DETAIL_ITEM1_RES_ID = R.id.player_voc_translation_detail;

    public static final int DEFAULT_PLAYER_DETAIL_ITEM2_RES_ID = R.id.player_voc_kk_detail;

    public static final int DEFAULT_PLAYER_DETAIL_ITEM3_RES_ID = R.id.player_voc_sentence_detail;

    public static final int DEFAULT_PLAYER_DETAIL_ITEM4_RES_ID = R.id.player_voc_sentence_translation_detail;


    public static final int DEFAULT_PLAYER_LIST_ITEM_COUNT = 2;

    public static final int DEFAULT_PLAYER_LIST_DETAIL_ITEM_COUNT = 5;

    private static final String TAG = PlayerScrollView.class.getSimpleName();

    private final float ZOOM_IN_FACTORY = 1.05f;

    private static final int DEFAULT_CHILD_COUNT_IN_SCROLL_VIEW = 6;

    private static final int DEFAULT_LIST_ITEM_FOCUSED_COLOR_RES_ID = R.color.player_list_item_focused_color;

    private static final int DEFAULT_LIST_ITEM0_COLOR_RES_ID = R.color.player_list_item0_border_bottom_color;

    private static final int DEFAULT_DETAILS_COLOR_RES_ID = R.color.player_details_color;

    private Context mContext;

    private MyScrollView mScrollView;

    /**
     * A ViewGroup LinearLayout will be the only child added to the PlayerView
     * */
    private LinearLayout mLinearLayout;

    /**
     * mItemDetailsLinearLayout will be added in the center of PlayerScrollView for showing the
     * details of the item in mCurrentFocusedPosition
     * */
    private LinearLayout mItemDetailsLinearLayout;

    private LinearLayout mTopGradientMask;

    private LinearLayout mBottomGradientMask;

    /**
     * The count of item that will be seen in the Screen
     * */
    private int mChildCountInScrollView;

    /**
     * Height for our PlayerView
     * */
    private int mPlayerViewHeight;

    /**
     * Width for our PlayerView
     * */
    private int mPlayerViewWidth;

    /**
     * Margin top for our PlayerView, default is twice of mChildViewHeight
     * */
    private int mPlayerViewTopMargin;

    /**
     * Margin bottom for our PlayerView, default is twice of mChildViewHeight
     * */
    private int mPlayerViewBottomMargin;

    /**
     * Width for our child
     * */
    private int mChildViewWidth;
    /**
     * Height for our child
     * */
    private int mChildViewHeight;

    private int mPlayerItemDetailWidth;

    private int mPlayerItemDetailHeight;

    /**
     * mChildViewZoomInWidth is used for focused child view
     * */
    private int mChildViewZoomInWidth;
    /**
     * mChildViewZoomInHeight is used for focused child view
     * */
    private int mChildViewZoomInHeight;
    /**
     * mCurrentFocusedPosition must be the item in the center of PlayerScrollView
     * */
    private int mCurrentFocusedPosition;

    private int mListItemCount;
    /**
     * PlayerAdapter is used for assigning different data to child view in viewgroup
     * */
    private PlayerAdapter mPlayerAdapter;

    private View mCurrentShowingDetailsItem;

    private boolean mShowingDetails;

    private String[] mDetailsFrom;

    private int[] mDetailsTo;

    private LinkedList<HashMap> mDetailsLL;

    private final Runnable checkFinalItemStateTask;

    private final Runnable checkInitialItemStateTask;

    private boolean mInitialItemCheckFlag;

    private boolean mFinalItemCheckFlag;

    public PlayerScrollView(Context context) {
        this(context, null);
    }

    public PlayerScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        Log.d(TAG, "on create");

        mContext = context;

        checkFinalItemStateTask = new Runnable() {
            @Override
            public void run() {
                if(getChildView(mListItemCount - 1) != null) {
                    mFinalItemCheckFlag = true;
                    if(mOnItemPreparedListener != null)
                        mOnItemPreparedListener.onFinalItemPrepared();
                }
                else
                    PlayerScrollView.this.postDelayed(checkFinalItemStateTask, 100);

            }
        };

        checkInitialItemStateTask = new Runnable() {
            @Override
            public void run() {
                if(getChildView(0) != null) {
                    mInitialItemCheckFlag = true;
                    if(mOnItemPreparedListener != null)
                        mOnItemPreparedListener.onInitialItemPrepared();
                    initFocusedPosition();
                }
                else
                    PlayerScrollView.this.postDelayed(checkInitialItemStateTask, 100);
            }
        };

        initPlayerView();

        addView(mScrollView);
        addView(mTopGradientMask);
        addView(mBottomGradientMask);

    }

    /**
     * This is important for PlayerScrollView,
     * Before the view is onDraw, system will first check the size.
     * Override onMeasure to tell how big our view we want.
     * If our custom view extends ViewGroup, must use MeasureSpec.makeMeasureSpec.
     * */

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(MeasureSpec.makeMeasureSpec(mPlayerViewWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mPlayerViewHeight, MeasureSpec.EXACTLY));
//    }

    /**
     * Initialize the PlayerView, make the view size is about 0.9 & 0.7 screen resolution.
     * Initialize the child size, make it equal to 1/CHILD_COUNT of its parent size
     * */
    private void initPlayerView(){

        setChildSize();
        setChildZoomInSize();
        setPlayerViewWithDefaultSize();

        mLinearLayout = new LinearLayout(mContext);
        LinearLayout.LayoutParams mLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mLinearLayout.setLayoutParams(mLayoutParams);
        mLinearLayout.setOrientation(LinearLayout.VERTICAL);
        mLinearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        LayoutTransition testT = new LayoutTransition();

        PropertyValuesHolder pvhScaleX = PropertyValuesHolder.ofFloat("scaleX", 0f, 1f);
        PropertyValuesHolder pvhScaleY = PropertyValuesHolder.ofFloat("scaleY", 0f, 1f);
        Animator appearAnimScale = ObjectAnimator.ofPropertyValuesHolder((Object)null ,pvhScaleX, pvhScaleY);
        appearAnimScale.setStartDelay(300);
        appearAnimScale.setDuration(300);
        appearAnimScale.setInterpolator(new AccelerateDecelerateInterpolator());
        testT.setAnimator(LayoutTransition.APPEARING, appearAnimScale);
        mLinearLayout.setLayoutTransition(testT);

        mTopGradientMask = new LinearLayout(mContext);
        RelativeLayout.LayoutParams mTopGradientMaskLayoutParams = new RelativeLayout.LayoutParams(mChildViewWidth, mChildViewHeight / 2);
        mTopGradientMaskLayoutParams.addRule(ALIGN_PARENT_TOP | CENTER_HORIZONTAL);
        mTopGradientMask.setLayoutParams(mTopGradientMaskLayoutParams);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mTopGradientMask.setBackground(getResources().getDrawable(R.drawable.top_gradient_filter, null));
        else
            mTopGradientMask.setBackground(getResources().getDrawable(R.drawable.top_gradient_filter));

        mBottomGradientMask = new LinearLayout(mContext);
        RelativeLayout.LayoutParams mBottomGradientMaskLayoutParams = new RelativeLayout.LayoutParams(mChildViewWidth, mChildViewHeight /2);
        mBottomGradientMaskLayoutParams.addRule(ALIGN_PARENT_BOTTOM);
        mBottomGradientMaskLayoutParams.addRule(CENTER_HORIZONTAL);
        mBottomGradientMask.setLayoutParams(mBottomGradientMaskLayoutParams);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBottomGradientMask.setBackground(getResources().getDrawable(R.drawable.bottom_gradient_filter, null));
        else
            mBottomGradientMask.setBackground(getResources().getDrawable(R.drawable.bottom_gradient_filter));
    }

    private void initFocusedPosition() {
        setCurrentFocusedPosition(0);
        if(getCurrentFocusedView().getBackground() instanceof ColorDrawable)
            ((ColorDrawable) getCurrentFocusedView().getBackground()).setColor(getResources().getColor(DEFAULT_LIST_ITEM_FOCUSED_COLOR_RES_ID));
        else
            ((GradientDrawable) getCurrentFocusedView().getBackground()).setColor(getResources().getColor(DEFAULT_LIST_ITEM_FOCUSED_COLOR_RES_ID));
        getCurrentFocusedView().setScaleX(ZOOM_IN_FACTORY);
    }

    private void setPlayerViewWithDefaultSize(){

        mChildCountInScrollView = DEFAULT_CHILD_COUNT_IN_SCROLL_VIEW;
        mPlayerViewHeight = mChildViewHeight * mChildCountInScrollView;
        mPlayerViewWidth = mChildViewZoomInWidth;
        mPlayerViewTopMargin = (int)(mChildViewHeight * (((float)DEFAULT_CHILD_COUNT_IN_SCROLL_VIEW - 1) / 2));
        mPlayerViewBottomMargin = mPlayerViewTopMargin;
        mPlayerItemDetailHeight = (int) (getContext().getResources().getDisplayMetrics().heightPixels * 0.28);
        mPlayerItemDetailWidth = mPlayerViewWidth;

        ViewGroup.LayoutParams viewGroupLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        setLayoutParams(viewGroupLayoutParams);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mPlayerViewWidth, mPlayerViewHeight);

        layoutParams.addRule(CENTER_HORIZONTAL);
        mScrollView = new MyScrollView(mContext);
        mScrollView.setLayoutParams(layoutParams);
    }

    /**
     * Use the getScrollY() to find out which position should be the focused. If we successfully
     * find this position out, the corresponding getCurrentFocusedView should be the view located
     * in the center of ScrollView
     * */
    private int findCurrentPosition(int coordinateY){

        /**
         * This parameter is used to find our currently center coordinateY. Because of the marginTop
         * we have set, we don't specific re-calculate centerCoordinate with adding half rect-height
         * e.g. if coordinateY = 0, the the parameter equals .5*ChildHeight, which will correctly
         * located at child view index0
         * */
        int centerRectCenterCoordinateY =
                coordinateY + (int) Math.abs(mChildViewHeight*0.5);

        /**
         * If the center coordinateY is beyond a childheight, current position will change as well
         * */
        return (centerRectCenterCoordinateY / mChildViewHeight);
    }

    public void moveToPosition(int position){
        mScrollView.smoothScrollTo(mScrollView.getScrollX(), position * mChildViewHeight);

    }

    /**
     * The method is used for setting current focused position based on scrollbar's position.
     * Normally, the item in the middle should be focused and the color will be orange.
     * */
    public void setCurrentFocusedPosition(int newPosition){

        if(newPosition >= mListItemCount || newPosition < 0)
            return;

        if(mShowingDetails)
            hideItemDetails();

        int previousFocusedPosition = mCurrentFocusedPosition;

        mCurrentFocusedPosition = newPosition;

        performItemAppearanceChanged(previousFocusedPosition);

    }


    private void performItemAppearanceChanged(int previousFocusedPosition){

        int focused_color = getResources().getColor(DEFAULT_LIST_ITEM_FOCUSED_COLOR_RES_ID);
        int item0_color = getResources().getColor(DEFAULT_LIST_ITEM0_COLOR_RES_ID);

        View newFocusedView = getCurrentFocusedView();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            newFocusedView.setElevation(30);

        ValueAnimator newFocusedViewAnim = ObjectAnimator.ofFloat(newFocusedView, "scaleX", 1.0f, ZOOM_IN_FACTORY);
        newFocusedViewAnim.setDuration(100);
        newFocusedViewAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        newFocusedViewAnim.start();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ValueAnimator newFocusedViewColorAnim = ObjectAnimator.ofArgb(newFocusedView, "BackgroundColor", item0_color, focused_color);
            newFocusedViewColorAnim.setDuration(100);
            newFocusedViewColorAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            newFocusedViewColorAnim.start();
        }
        else{
            newFocusedView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.player_item0));
        }

        if(previousFocusedPosition < 0)
            return;

        final View previousFocusedView = getChildView(previousFocusedPosition);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            previousFocusedView.setElevation(0);
        }

        ValueAnimator previousFocusedViewAnim = ObjectAnimator.ofFloat(previousFocusedView, "scaleX", ZOOM_IN_FACTORY, 1.0f);
        previousFocusedViewAnim.setDuration(100);
        previousFocusedViewAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        previousFocusedViewAnim.start();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ValueAnimator previousFocusedViewColorAnim = ObjectAnimator.ofArgb(previousFocusedView, "BackgroundColor", focused_color, item0_color);
            previousFocusedViewColorAnim.setDuration(100);
            previousFocusedViewColorAnim.setInterpolator(new AccelerateDecelerateInterpolator());
            previousFocusedViewColorAnim.start();
            previousFocusedViewColorAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    previousFocusedView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.player_item1));
//                previousFocusedView.setBackground(mContext.getResources().getDrawable(R.drawable.player_item1, null));
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        }
        else
            previousFocusedView.setBackground(ContextCompat.getDrawable(mContext, R.drawable.player_item1));
    }

    /**
     * showItemDetails shows the detail of each item
     * it will do the following thing
     * 1. Add a new faked layout (fakedLinearLayout) which size is the same with ZoomIn item (Immediately)
     * and make the faked layout stretch to the larger size slowly (0.5s)
     * 2. Show the detail content of word (0.3s)
     * */
    public void showItemDetails(){
        ArrayList<String> arrayList = new ArrayList<>();
        if(mItemDetailsLinearLayout == null) {

            arrayList.add("TestTestTestTest");
            arrayList.add("Test222222222222");
            mItemDetailsLinearLayout = new ItemDetailLinearLayout(mContext, 2, arrayList, arrayList);
        }
        else
            return;

        mCurrentShowingDetailsItem = getCurrentFocusedView();

        // Step 1

//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mPlayerItemDetailWidth, mPlayerItemDetailHeight);
        layoutParams.addRule(CENTER_HORIZONTAL);
//        layoutParams.setMargins(0, mPlayerViewTopMargin - (550 - mChildViewHeight) / 2, 0, 0);
        layoutParams.setMargins(0, mPlayerViewTopMargin - (mPlayerItemDetailHeight - mChildViewHeight) / 2, 0, 0);
        mItemDetailsLinearLayout.setLayoutParams(layoutParams);

        mItemDetailsLinearLayout.setBackgroundColor(getResources().getColor(DEFAULT_DETAILS_COLOR_RES_ID));

//        PropertyValuesHolder stretchVH = PropertyValuesHolder.ofFloat("scaleY", ((float) mChildViewZoomInHeight) / 550, 1f);
                PropertyValuesHolder stretchVH = PropertyValuesHolder.ofFloat("scaleY", ((float) mChildViewZoomInHeight) / mPlayerItemDetailHeight, 1f);
        PropertyValuesHolder elevateVH = PropertyValuesHolder.ofFloat("Elevation", 0, 40);
        Animator animator = ObjectAnimator.ofPropertyValuesHolder(mItemDetailsLinearLayout, stretchVH, elevateVH);
        animator.setDuration(500);

        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setAnimator(LayoutTransition.APPEARING, animator);
        setLayoutTransition(layoutTransition);


        // step 2

        addView(mItemDetailsLinearLayout);
        View detailContentView = mItemDetailsLinearLayout.getChildAt(0);
        ValueAnimator detailAppearAnim = ObjectAnimator.ofFloat(detailContentView, "alpha", 0f, 1f);
        detailAppearAnim.setStartDelay(500);
        detailAppearAnim.setDuration(300).start();

        mItemDetailsLinearLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mShowingDetails)
                    hideItemDetails();
            }
        });

        mShowingDetails = true;
    }

    /**
     * hideItemDetails shows the detail of each item
     * it will do the following thing
     * 1. Make the detail's content disappear slowly (0.3s)
     * 2. Make faked layout (fakedLinearLayout) ZoomOut and disappear (0.5s)
     * 3. Change Color and show the detail content of word (0.3s)
     * */
    public void hideItemDetails(){

        if(mItemDetailsLinearLayout == null)
            return;

        AnimatorSet animatorSet = new AnimatorSet();

        View detailContentView = mItemDetailsLinearLayout.getChildAt(0);
        ValueAnimator detailDisappearAnim = ObjectAnimator.ofFloat(detailContentView, "alpha", 1.0f, 0f);
        detailDisappearAnim.setDuration(300);

        ValueAnimator detailZoomOutAnim = ObjectAnimator.ofFloat(mItemDetailsLinearLayout, "ScaleY", 1.0f, ((float) mChildViewZoomInHeight) / 500);
        detailDisappearAnim.setDuration(500);

        animatorSet.play(detailDisappearAnim).before(detailZoomOutAnim);
        animatorSet.start();
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                removeView(mItemDetailsLinearLayout);
                mItemDetailsLinearLayout = null;
                mShowingDetails = false;
                mCurrentShowingDetailsItem = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    public PlayerAdapter getAdapter(Context context,int resource, LinkedList<HashMap> dataList, String[] from, int[] to){

        mDetailsLL = dataList;
        mDetailsFrom = from;
        mDetailsTo = to;
        mListItemCount = mDetailsLL.size();

        mPlayerAdapter = new PlayerAdapter(context, resource, dataList, from, to);

        return mPlayerAdapter;

    }

    public void setAdapter(final PlayerAdapter adapter){
        mScrollView.removeAllViews();
        mScrollView.addView(mLinearLayout);
        adapter.setChildViewtoGroup();
        mCurrentFocusedPosition = -1;

        checkInitialItemStateTask.run();
        checkFinalItemStateTask.run();

    }

    private void setChildSize(){
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mChildViewHeight = (int) Math.floor(displayMetrics.heightPixels*0.125);
        mChildViewWidth = (int) Math.floor(displayMetrics.widthPixels*0.90);
    }

    private void setChildZoomInSize(){
        mChildViewZoomInHeight = mChildViewHeight;
        mChildViewZoomInWidth = (int) Math.floor(mChildViewWidth*ZOOM_IN_FACTORY);
    }

    public void setOnPlayerScrollStoppedListener(OnPlayerScrollStoppedListener listener){
        mOnPlayerScrollStoppedListener = listener;
    }

    public void setOnFocusChangedListener(OnFocusChangedListener listener) {
        mOnFocusChangedListener = listener;
    }

    public void setOnItemPreparedListener(OnItemPreparedListener listener){
        mOnItemPreparedListener = listener;
    }

    public View getChildView(int index){
        return mLinearLayout.getChildAt(index);
    }

    public int getPlayerViewHeight(){
        return mPlayerViewHeight;
    }

    public int getPlayerViewWidth(){
        return mPlayerViewWidth;
    }

    public int getChildViewWidth(){
        return mChildViewWidth;
    }

    public int getChildViewHeight(){
        return mChildViewHeight;
    }

    public int getChildViewZoomInWidth(){
        return mChildViewZoomInWidth;
    }

    public int getChildViewZoomInHeight(){
        return mChildViewZoomInHeight;
    }

    public int getPlayerViewTopMargin(){
        return mPlayerViewTopMargin;
    }

    public int getPlayerViewBottomMargin(){
        return mPlayerViewBottomMargin;
    }

    public View getCurrentFocusedView(){
        return mLinearLayout.getChildAt(mCurrentFocusedPosition);
    }

    public boolean getInitialItemCheck(){
        return mInitialItemCheckFlag;
    }

    public boolean getFinalItemCheck(){
        return mFinalItemCheckFlag;
    }

    /**
     * The method used for returning the currently being focused view's position, typically means
     * the view's index in the scrollView center
     */
    public int getCurrentFocusedPosition(){
        return mCurrentFocusedPosition;
    }

    public int getCurrentFocusedPositionCoordinateY(){
        return mCurrentFocusedPosition*mChildViewHeight;
    }

    public int getCoordinateYByPosition(int position){
        return position*mChildViewHeight;
    }

    private class ItemDetailLinearLayout extends LinearLayout{

        private Context context;

        private WrapContentViewPager viewPager;

        private LinkedList<ViewGroup> mItemPagesList;

        public ItemDetailLinearLayout(Context context,
                                      int pageCount,
                                      ArrayList<String> en_sentenceList,
                                      ArrayList<String> cn_sentenceList) {
            super(context);
            this.context = context;
            setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            setOrientation(VERTICAL);
            ViewGroup itemView = (ViewGroup)((LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.player_layout_details, null);
            viewPager = new WrapContentViewPager(context);
            addView(itemView);
            ((ViewGroup)itemView.findViewById(R.id.pager_parent)).addView(viewPager);

            createItemPages(pageCount, en_sentenceList, cn_sentenceList);

        }

        private void createItemPages(int pageCount, ArrayList<String> en_sentenceList,
                                     ArrayList<String> cn_sentenceList){


            mItemPagesList = new LinkedList<>();
            for(int i = 0; i < pageCount; i++) {
                ViewGroup currentItemDetailsView =
                        (ViewGroup)((LayoutInflater) getContext()
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                                .inflate(R.layout.player_layout_details_sentence, null);

                ((TextView) currentItemDetailsView.findViewById(R.id.player_voc_sentence_detail))
                        .setText(en_sentenceList.get(i));
                ((TextView) currentItemDetailsView.findViewById(R.id.player_voc_sentence_translation_detail))
                        .setText(cn_sentenceList.get(i));

                mItemPagesList.add(currentItemDetailsView);
            }
            viewPager.setAdapter(new LinkedListPagerAdapter(mItemPagesList));
        }

    }

    class MyScrollView extends ScrollView{

        private Runnable scrollerTask;
        private OnScrollStoppedListener onScrollStoppedListener;
        private int newCheck = 100;
        private int initialPosition;

        public MyScrollView(Context context) {
            super(context);

            onScrollStoppedListener = new OnScrollStoppedListener();

            scrollerTask = new Runnable() {
                @Override
                public void run() {
                    int newPosition = getScrollY();
                    Log.d("PlayerScrollView", " " + initialPosition + " " +newPosition);
                    if(initialPosition - newPosition == 0){//has stopped

                        if(onScrollStoppedListener!=null){
                            onScrollStoppedListener.onScrollStopped(newPosition);
                        }
                    }else{
                        initialPosition = getScrollY();
                        MyScrollView.this.postDelayed(scrollerTask, newCheck);
                    }
                }
            };

        }

        /**
         * l : scroll bar x coordinate
         * t : scroll bar y coordinate
         * */
        @Override
        protected void onScrollChanged(int l, int t, int oldl, int oldt) {
            super.onScrollChanged(l, t, oldl, oldt);
            int newPosition;
            if ((newPosition = findCurrentPosition(t)) != mCurrentFocusedPosition)
                setCurrentFocusedPosition(newPosition);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if(!mInitialItemCheckFlag)
                return super.onTouchEvent(ev);
            switch (ev.getAction()) {
                case MotionEvent.ACTION_UP:
                    startScrollerTask();
                    break;
                default:
                    break;
            }

            return super.onTouchEvent(ev);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            if(ev.getAction() == MotionEvent.ACTION_DOWN){
                if (mShowingDetails || !getInitialItemCheck())
                    return false;
//                if(mShowingDetails)
//                    return false;
            }
            return super.onInterceptTouchEvent(ev);
        }

        public void startScrollerTask(){

            initialPosition = getScrollY();
            MyScrollView.this.postDelayed(scrollerTask, newCheck);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(mPlayerViewWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mPlayerViewHeight, MeasureSpec.EXACTLY));
        }

        class OnScrollStoppedListener{
            public void onScrollStopped(int scrollEndPositionY){
                int offset = scrollEndPositionY % mChildViewHeight;
                if(offset > Math.abs(mChildViewHeight*0.5)){
                    offset = mChildViewHeight - offset;
                    smoothScrollTo(getScrollX(), scrollEndPositionY + offset);
                }
                else
                    smoothScrollTo(getScrollX(), scrollEndPositionY - offset);

                int newPosition;
                if((newPosition = findCurrentPosition(getScrollY())) != mCurrentFocusedPosition)
                    setCurrentFocusedPosition(newPosition);

                mOnPlayerScrollStoppedListener.onPlayerScrollStopped();
            }
        }
    }

    class PlayerAdapter{

        private Context mContext;
        private ViewGroup mParent;
        private int mResource;
        private LinkedList<HashMap> mDataList;
        private String[] mFrom;
        private int[] mTo;

        private LayoutInflater mInflater;

        PlayerAdapter(Context context,int resource, LinkedList<HashMap> dataList, String[] from, int[] to){
            mContext = context;

            mParent = mLinearLayout;

            mDataList = dataList;
            mResource = resource;
            mFrom = from;
            mTo = to;

            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setChildViewtoGroup(){

            mParent.removeAllViews();

            Iterator<HashMap> ii = mDataList.iterator();
            int dataCount = mDataList.size();
            int itemFilledStartIndex = 0;
            int itemFilledEndIndex = DEFAULT_PLAYER_LIST_ITEM_COUNT;

            int index = -1;
            while(ii.hasNext()){
                index++;

                final RelativeLayout v = (RelativeLayout) mInflater.inflate(mResource, mParent, false);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getChildViewWidth(), getChildViewHeight());
                if(index == 0)
                    layoutParams.setMargins(0, mPlayerViewTopMargin, 0, 0);
                else if(index == dataCount - 1)
                    layoutParams.setMargins(0, 0, 0, mPlayerViewBottomMargin);
                else
                    layoutParams.setMargins(0, 0, 0, 0);


                v.setLayoutParams(layoutParams);
                v.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = mParent.indexOfChild(v);
                        if (position != mCurrentFocusedPosition) {
                            if(mShowingDetails)
                                hideItemDetails();
                        }
                        else
                            showItemDetails();

                    }
                });

                HashMap<String, String> dataMap = ii.next();
                for(int i = itemFilledStartIndex; i < itemFilledEndIndex; i++){
                    TextView textView = (TextView) v.findViewById(mTo[i]);
                    textView.setText(dataMap.get(mFrom[i]));
                }
//                Log.d("PlayerScrollView", " "+index);


                mParent.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mParent.addView(v);
                    }
                }, index * 100);
            }
        }


    }

    /**
     * WrapContentViewPager enables ViewPager can be assigned a specific size.
     * */
    private class WrapContentViewPager extends ViewPager {

        public WrapContentViewPager(Context context) {
            super(context);
        }

//        @Override
//        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//
//            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mPlayerItemDetailHeight, MeasureSpec.EXACTLY);
//            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mPlayerItemDetailWidth, MeasureSpec.EXACTLY);
//            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        }



    }
}
