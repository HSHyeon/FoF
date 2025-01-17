package com.FoF.FoF_Android.home;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.FoF.FoF_Android.HttpClient;
import com.FoF.FoF_Android.OnboardingActivity;
import com.FoF.FoF_Android.R;
import com.FoF.FoF_Android.RetrofitApi;
import com.FoF.FoF_Android.TokenManager;
import com.FoF.FoF_Android.detail.DetailFragment;
import com.FoF.FoF_Android.home.model.Meme;
import com.FoF.FoF_Android.home.model.MemeResponse;
import com.FoF.FoF_Android.home.view.CustomSwipeableViewPager;
import com.FoF.FoF_Android.home.view.StackPageTransformer;
import com.FoF.FoF_Android.search.EndlessScrollListener;
import com.baoyz.widget.PullRefreshLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements OnItemClick, FragmentManager.OnBackStackChangedListener {
    TabLayout tabLayout;
    private RecyclerView recycle;
    RetrofitApi api;
    Integer tabid=0;
    View view1;
    List<Meme.Data> pitems, items;
    Integer cposition;
    Integer viewitem;
    Integer i=1 ,j=0;
    private static final int MAX_SIZE = 15;
    private static final int ALL_MAX_SIZE = 10;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    MemeAllAdapter adapter;
    FrameLayout container, pagercontainer;
    ViewGroup view;
    TokenManager gettoken;
    CustomSwipeableViewPager myviewpager;
    MemePagerAdapter padapter;
    GestureDetector gestureDetector;
    List<Meme.Data> plusitem;
    View.OnTouchListener gestureListener;
    String token;
    private int mNumber = 0;


    public HomeFragment() {

    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gettoken=new TokenManager(getContext());
        token = gettoken.checklogin(getContext());
        System.out.println("확인" + token);

        HttpClient client = new HttpClient();
        api = client.getRetrofit().create(RetrofitApi.class);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);

        recycle = view.findViewById((R.id.recycler));
        myviewpager=view.findViewById(R.id.myviewpager);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        // Check if we need to display our OnboardingSupportFragment
        if (!sharedPreferences.getBoolean(
                "onBoarding", false)) {
            OnboardingActivity reportDialog=new OnboardingActivity(getContext());
            reportDialog.setCancelable(true);
            reportDialog.getWindow().setGravity(Gravity.CENTER);
            reportDialog.show();
        }
        PullRefreshLayout  mSwipeRefreshLayout = (PullRefreshLayout) view.findViewById(R.id.swipe_layout);

        mSwipeRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_WATER_DROP);
        mSwipeRefreshLayout.setRefreshDrawable(new CirclesDrawable(getContext(),mSwipeRefreshLayout));
        mSwipeRefreshLayout.setColorSchemeColors(Color.parseColor("#FE2C55"),Color.parseColor("#BEDCFF"),Color.parseColor("#07C87B"),Color.parseColor("#FE2C55"));
       // mSwipeRefreshLayout.setScrollBarSize();
        mSwipeRefreshLayout.setOnRefreshListener(new PullRefreshLayout .OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getUploadData();
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                },1000);

            }
        });
       // initUI();
        myviewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.i("main","현재위치"+position);
                if(plusitem!=null) {
                    if(((position) % MAX_SIZE == MAX_SIZE-1 )&& (plusitem.size() == MAX_SIZE)) {initPagerUI(++i);Log.i("test1","여기"+i);}
                    else if(plusitem.size()!=MAX_SIZE) ;//i--;
                }
                else if (i==1){if((position-1)%MAX_SIZE==0) initPagerUI(++i);
                    Log.i("test2","여기"+i);}

                viewitem = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        myviewpager.setPageTransformer(true, new StackPageTransformer(myviewpager));
        tabLayout =view.findViewById(R.id.tabLayout) ;
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                setCurrentTabFragment(position,view);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

      //  getContext().getContentResolver().delete(myurl,null,null);
        return view;
    }

    public void setCurrentTabFragment(int position, ViewGroup view){

        pagercontainer=(FrameLayout)view.findViewById(R.id.pagercontainer);
        container=(FrameLayout)view.findViewById(R.id.container);
        //  container.setVisibility(View.GONE);
        switch (position)
        {
            case 0 :
                tabid=0;
                container.setVisibility(View.GONE);
                pagercontainer.setVisibility(View.VISIBLE);
                myviewpager.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if(viewitem!=null)myviewpager.setCurrentItem(viewitem);
                    }
                }, 100);

                break;
            case 1 :
                tabid=1;

                container.setVisibility(View.VISIBLE);
                pagercontainer.setVisibility(View.GONE);
                break;
        }
    }


    public void setadapter(List<Meme.Data> items) {
        adapter = new MemeAllAdapter(items,getContext());
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recycle.setLayoutManager(layoutManager);
        recycle.setAdapter(adapter);
        adapter.setOnItemClickListener(new MemeAllAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Meme.Data item = adapter.getItem(position);
                DetailFragment detail = new DetailFragment(item.getMemeIdx());
                getFragmentManager().beginTransaction().addSharedElement(v.findViewById(R.id.imageView), ViewCompat.getTransitionName(v.findViewById(R.id.imageView)))
                        .setReorderingAllowed(true)
                        .addToBackStack(null).replace(R.id.container, detail).commit();
            }
        });


 }
    private void initUI() {
        setadapter(items);
        EndlessScrollListener scrollListener = new EndlessScrollListener(new EndlessScrollListener.RefreshList() {
            @Override
            public void onRefresh(int pageNumber) {
                Call<MemeResponse> call = api.getdata(token, "all", getPage(0), ALL_MAX_SIZE); //page설정
                call.enqueue(new Callback<MemeResponse>() {
                     @Override
                     public void onResponse(Call<MemeResponse> call, Response<MemeResponse> response) {
                         if (response.isSuccessful()) {
                             List<Meme.Data> plusrecycler = response.body().getItems();
                             items.addAll(plusrecycler);
                             adapter.notifyItemInserted(items.size() - 1);
                             adapter.notifyDataSetChanged();
                         }
                     }

                     @Override
                     public void onFailure(Call<MemeResponse> call, Throwable t) {
                     }
                });
            }
        });
        recycle.addOnScrollListener(scrollListener);
    }
    public void getUploadData(){
        HttpClient client=new HttpClient();
        api = client.getRetrofit().create(RetrofitApi.class);
        String token = gettoken.checklogin(getContext());
        j=0;
        api.getdata(token, "all", getPage(0), ALL_MAX_SIZE).enqueue(new Callback<MemeResponse>() {
            @Override
            public void onResponse(Call<MemeResponse> call, Response<MemeResponse> response) {
                 items = response.body().getItems();

                 setadapter(items);
                recycle.setHasFixedSize(true);
                recycle.setItemViewCacheSize(20);
                // 먼저 업로드로 리사이클러뷰를 세팅
                initUI();
            }
            @Override
            public void onFailure(Call<MemeResponse> call, Throwable t) {
            }
        });
    }
    public int getPage(int flag){
            j ++;
            return j;
    }

    private void initPagerUI(int i) {
        Call<MemeResponse> call = api.getdata(token,"recommend",i,MAX_SIZE);
        call.enqueue(new Callback<MemeResponse>() {
            @Override
            public void onResponse(Call<MemeResponse> call, Response<MemeResponse> response) {
                if(response.isSuccessful()){

                    plusitem = response.body().getItems();

                    Log.i("TAG", "onResponse3: "+i);
                    if(i==1)  {pitems=plusitem; setPageradapter(plusitem);}
                    else pluspager(pitems.size(), plusitem);
                }
                else
                    Log.i("TAG", "onResponse1: "+response.code());
            }

            @Override
            public void onFailure(Call<MemeResponse> call, Throwable t) {

                Log.d("MainActivity", t.toString());
            }
        });


    }
    public  void setPageradapter(List<Meme.Data> pitems){

        Integer idx= gettoken.checkIdx(getContext());
        padapter=new MemePagerAdapter(getContext(),idx,pitems,HomeFragment.this::onClick);
        //  myviewpager.setOnDragListener();
        padapter.setOnItemClickListener(new MemePagerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, String position) {

                HashClickFragment hashclick= HashClickFragment.newInstance(position);
                hashclick.setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.slide_bottom).setDuration(200));
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.container, hashclick).addToBackStack(null).commit();
            }
        });

        gestureDetector = new GestureDetector(getContext(), new MyGestureDetector());


        padapter.setOnTouchListener(new MemePagerAdapter.OnTouchListener() {
            @Override
            public boolean onTouch(View v, Integer position, MotionEvent event) {
                cposition=position;
                view1=v;
                return gestureDetector.onTouchEvent(event);
            }
        });
        myviewpager.setOnTouchListener(gestureListener);
        myviewpager.setOffscreenPageLimit(12);
        myviewpager.setAdapter(padapter);
    }

    private void pluspager( int index, List<Meme.Data> plusitems) {
            pitems.addAll(index,plusitems);
            Log.i("tag","호출되는 곳");
            padapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(String value) {
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, HashClickFragment.newInstance(value)).addToBackStack(null).commit();

    }

    @Override
    public void onBackStackChanged() {

    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    DetailFragment detail = new DetailFragment(pitems.get(cposition).getMemeIdx());
                    detail.setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(R.transition.image_shared_element_transition));
                    //detail.setEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.fade));

                    getFragmentManager().beginTransaction().setReorderingAllowed(true).addSharedElement(view1.findViewById(R.id.imageView),ViewCompat.getTransitionName(view.findViewById(R.id.imageView)))
                            .addToBackStack(null).replace(R.id.container, detail).commit();
                    }
            } catch (Exception e) {
                // nothing
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if(pitems!=null) setPageradapter(pitems);
        else {
            i=1;
            initPagerUI(i);
        }
        if(tabid!=3) {setCurrentTabFragment(tabid,view);
            TabLayout.Tab tab=tabLayout.getTabAt(tabid);
            tab.select();
        }

         getUploadData();
        Log.i("test",i.toString());


    }


}