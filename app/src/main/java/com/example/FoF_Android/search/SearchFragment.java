package com.example.FoF_Android.search;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.FoF_Android.HttpClient;
import com.example.FoF_Android.R;
import com.example.FoF_Android.RetrofitApi;
import com.example.FoF_Android.TokenManager;
import com.google.android.material.tabs.TabLayout;
import com.ramotion.expandingcollection.ECCardData;
import com.ramotion.expandingcollection.ECPagerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {
    TabLayout tabLayout;
    NonSwipeViewPager viewPager;
    RetrofitApi api;
    TokenManager gettoken;
    TextView hash1;

    int tagIdx[] = new int[5];
    String tagName[] = new String[5];
    int searchCnt[] = new int[5];



    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(String param1, String param2) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = HttpClient.getRetrofit().create(RetrofitApi.class);
        gettoken = new TokenManager(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);
        hash1 = view.findViewById(R.id.hash1);
        getHashTag(api);

        tabLayout = (TabLayout)view.findViewById(R.id.searchTabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("감정"));
        tabLayout.addTab(tabLayout.newTab().setText("동물"));
        tabLayout.addTab(tabLayout.newTab().setText("상황"));
        tabLayout.addTab(tabLayout.newTab().setText("클립"));
        tabLayout.addTab(tabLayout.newTab().setText("텍스트"));
        tabLayout.addTab(tabLayout.newTab().setText("이모티콘"));

        viewPager = (NonSwipeViewPager)view.findViewById(R.id.viewPager);
        viewPager.setAdapter(new PagerAdapter(getFragmentManager()));
        viewPager.setPagingEnabled(false);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });


        RecyclerView mRecyclerView = view.findViewById(R.id.hashtag_recycler);


        return view;
    }

    public void getHashTag(RetrofitApi api){
        String token = gettoken.checklogin(getContext());
        api.getTag(token).enqueue(new Callback<HashTag>() {
            @Override
            public void onResponse(Call<HashTag> call, Response<HashTag> response) {
                HashTag tag = response.body();
                for(int i=0; i<tag.getData().getTagList().size(); i++){
                    tagIdx[i] = tag.getData().getTagList().get(i).getTagIdx();
                    tagName[i] = tag.getData().getTagList().get(i).getTagName();
                    searchCnt[i] = tag.getData().getTagList().get(i).getSearchCnt();
                }
                hash1.setText(tagName[0]);
            }

            @Override
            public void onFailure(Call<HashTag> call, Throwable t) {

            }
        });
    }
}