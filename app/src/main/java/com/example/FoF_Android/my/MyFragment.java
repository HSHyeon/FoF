package com.example.FoF_Android.my;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.bumptech.glide.Glide;
import com.example.FoF_Android.HttpClient;
import com.example.FoF_Android.R;
import com.example.FoF_Android.RetrofitApi;
import com.example.FoF_Android.TokenManager;
import com.example.FoF_Android.detail.DetailFragment;
import com.example.FoF_Android.dialog.LogoutDialog;
import com.example.FoF_Android.dialog.ProfileImgDialog;
import com.example.FoF_Android.make.RealPathUtil;
import com.example.FoF_Android.make.Utility;
import com.example.FoF_Android.search.HashSearch;
import com.example.FoF_Android.search.HashSearchAdapter;
import com.example.FoF_Android.search.HashTagAdapter;
import com.example.FoF_Android.signup.SignUp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.annotation.Target;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFragment extends Fragment {

    private ImageButton settingBt;
    private TextView nicktv;
    private CircleImageView profImage;
    private TextView liketv;
    private TextView liketv2;
    private TextView upLoadtv;
    private TextView upLoadtv2;
    private TextView acceptedLiketv;
    private TextView changeProfileImage;
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager layoutManager;
    private UploadLikeAdapter mAdapter;
    private UploadLikeAdapter mAdapter2;
    private TextView[] toptv = new TextView[5];
    String profileImage;
    private int likepage = 0;
    private int uploadpage = 0;
    private boolean isLoading = false;
    private int SELECT_FILE = 3;
    RetrofitApi api;
    TokenManager gettoken;
    String token;
    View view;

    private String userChoosenTask;
    InputStream in;
    File f;
    private ProfileImgDialog profileImgDialog;
    private LogoutDialog logoutDialog;
    Uri imageUri;
    String imagePath;
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3 s3;
    TransferUtility transferUtility;
    ByteArrayOutputStream stream;

    public MyFragment() {
        // Required empty public constructor
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
        view = inflater.inflate(R.layout.fragment_my, container, false);

        settingBt = view.findViewById(R.id.setting_bt);
        nicktv = view.findViewById(R.id.nick);
        profImage = view.findViewById(R.id.circleImageView);
        acceptedLiketv = view.findViewById(R.id.acceptedLike);
        changeProfileImage = view.findViewById(R.id.changeProfileImage);
        upLoadtv = view.findViewById(R.id.myUploadtv);
        upLoadtv2 = view.findViewById(R.id.myUploadtv2);
        liketv = view.findViewById(R.id.myLiketv);
        liketv2 = view.findViewById(R.id.myLiketv2);
        mRecyclerView = view.findViewById(R.id.profileRecyclerView);
        toptv[0] = view.findViewById(R.id.top1);    toptv[1] = view.findViewById(R.id.top2);    toptv[2] = view.findViewById(R.id.top3);    toptv[3] = view.findViewById(R.id.top4);    toptv[4] = view.findViewById(R.id.top5);

        token = gettoken.checklogin(getContext());

        upLoadtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUploadData(mAdapter);
                upLoadtv.setTextColor(Color.BLACK);
                upLoadtv2.setTextColor(Color.BLACK);
                liketv.setTextColor(Color.GRAY);
                liketv2.setTextColor(Color.GRAY);
            }
        });
        changeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               profileImgDialog= new ProfileImgDialog(getContext(),mdeletelistener, mselectlistener);
                profileImgDialog.setCancelable(true);
                profileImgDialog.getWindow().setGravity(Gravity.CENTER);

                profileImgDialog.show();

            }
        });
        liketv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLikeData(mAdapter2);
                upLoadtv.setTextColor(Color.GRAY);
                upLoadtv2.setTextColor(Color.GRAY);
                liketv.setTextColor(Color.BLACK);
                liketv2.setTextColor(Color.BLACK);
            }
        });

        settingBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }


    public View.OnClickListener mdeletelistener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            profileImgDialog.dismiss();
            logoutDialog=new LogoutDialog(2,getContext(),mnegativelistener);
            logoutDialog.setCancelable(true);
            logoutDialog.getWindow().setGravity(Gravity.CENTER);
            logoutDialog.show();
        }
    };
    public View.OnClickListener mnegativelistener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            logoutDialog.dismiss();
            logoutDialog.dismiss();
            String imgurl=Uri.parse("R.drawable.logo_big2").toString();

            api.patchprofileimg(gettoken.checklogin(getContext()),imgurl).enqueue(new Callback<SignUp>() {
                @Override
                public void onResponse(Call<SignUp> call, Response<SignUp> response) {
                    SignUp body = response.body();
                    Log.i("Myfragment",body.getMessage());
                }

                @Override
                public void onFailure(Call<SignUp> call, Throwable t) {

                }
            });

            Glide.with(getContext()).load(R.drawable.logo_big2).into(profImage);
        }
    };

    public View.OnClickListener mpositivelistener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            logoutDialog.dismiss();
            uploadImg();
            String imgurl="https://fofuploadtest.s3.ap-northeast-2.amazonaws.com/"+gettoken.checkIdx(getContext()).toString()+"prof"+f.getName();
            //TODO 수정 처리
            api.patchprofileimg(gettoken.checklogin(getContext()),imgurl).enqueue(new Callback<SignUp>() {
                @Override
                public void onResponse(Call<SignUp> call, Response<SignUp> response) {
                    SignUp body = response.body();
                    Log.i("Myfragment",body.getMessage());
                }

                @Override
                public void onFailure(Call<SignUp> call, Throwable t) {

                }
            });

        }
    };

    public View.OnClickListener mselectlistener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            profileImgDialog.dismiss();
            Boolean result = Utility.checkPermission(getContext());
            if(result)  galleryIntent();

        }
    };

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }


    @Override
    public void onResume() {
        super.onResume();
        System.out.println("여기실행");
        getProfile(api, token, view);
    }

    public void getProfile(RetrofitApi api, String token, View view){
        api.getMyProfile(token).enqueue(new Callback<MyProfile>() {
            @Override
            public void onResponse(Call<MyProfile> call, Response<MyProfile> response) {
                MyProfile body = response.body();
                profileImage = body.getdata().getProfile().getProfileImage();
                String nickName = body.getdata().getProfile().getNickname();
                int acceptedLike = body.getdata().getProfile().getAcceptedLikeCnt();
                int uploadCnt = body.getdata().getProfile().getUploadCnt();
                int likeCnt = body.getdata().getProfile().getLikeCnt();
                List<MyProfile.Data.Insight> mList = body.getdata().getInsight();

                setProfile(profileImage, nickName, acceptedLike, uploadCnt, likeCnt, mList, view);
                getUploadData(api, token, view);
                getLikeData(api, token, view);
            }

            @Override
            public void onFailure(Call<MyProfile> call, Throwable t) {

            }
        });
    }

    public void setProfile(String profileImage, String nickName, int acceptedLike, int uploadCnt, int likeCnt, List<MyProfile.Data.Insight> mList, View view) {
        Glide.with(getContext()).load(profileImage).error(R.drawable.logo_big2).into(profImage);
        nicktv.setText(nickName);
        acceptedLiketv.setText("" + acceptedLike);
        upLoadtv.setText("" + uploadCnt);
        liketv.setText("" + likeCnt);
        for (int i = 0; i < mList.size(); i++) {
            toptv[i].setText(mList.get(i).getTagName());
        }
        if (stream != null) {
            Glide.with(getContext())
                    .load(stream.toByteArray())
                    //   .placeholder(R.drawable.meme2)
                    .into(profImage);
            stream=null;
        }
    }
    public void getUploadData(RetrofitApi api, String token, View view){
        uploadpage=1;
        api.getUploadLike(token, "uploaded", uploadpage, 10).enqueue(new Callback<UploadLike>() {
            @Override
            public void onResponse(Call<UploadLike> call, Response<UploadLike> response) {
                UploadLike body = response.body();
                List<UploadLike.Data> mList = body.getData();

                mAdapter = new UploadLikeAdapter(mList, getContext());
                // 먼저 업로드로 리사이클러뷰를 세팅
                setUploadData(mAdapter);
            }

            @Override
            public void onFailure(Call<UploadLike> call, Throwable t) {

            }
        });
    }

    public void setUploadData(UploadLikeAdapter mAdapter){
        layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new HashTagAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                UploadLike.Data item = mAdapter.getItem(position);
                DetailFragment detail = new DetailFragment(item.getMemeIdx());
                getFragmentManager().beginTransaction().addSharedElement(v.findViewById(R.id.imageView), ViewCompat.getTransitionName(v.findViewById(R.id.imageView)))
                        .setReorderingAllowed(true)
                        .addToBackStack(null).replace(R.id.container, detail).commit();
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisibleItems;
                int[] firstVisibleItems = null;

                // 스크롤시 로딩 구현예정 ( 이미지 많이 등록 후 실험 )
                if(!isLoading){
                    firstVisibleItems = layoutManager.findFirstVisibleItemPositions(firstVisibleItems);
                    if(firstVisibleItems != null && firstVisibleItems.length > 0)
                        pastVisibleItems = firstVisibleItems[0];
                }
            }
        });
    }

    public void getLikeData(RetrofitApi api, String token, View view){
        likepage = 1;
        api.getUploadLike(token, "favorite", likepage, 10).enqueue(new Callback<UploadLike>() {
            @Override
            public void onResponse(Call<UploadLike> call, Response<UploadLike> response) {
                UploadLike body = response.body();
                List<UploadLike.Data> mList = body.getData();
                mAdapter2 = new UploadLikeAdapter(mList, getContext());
            }

            @Override
            public void onFailure(Call<UploadLike> call, Throwable t) {

            }
        });
    }

    public void setLikeData(UploadLikeAdapter mAdapter){
        layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(new HashTagAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                UploadLike.Data item = mAdapter.getItem(position);
                DetailFragment detail = new DetailFragment(item.getMemeIdx());
                getFragmentManager().beginTransaction().addSharedElement(v.findViewById(R.id.imageView), ViewCompat.getTransitionName(v.findViewById(R.id.imageView)))
                        .setReorderingAllowed(true)
                        .addToBackStack(null).replace(R.id.container, detail).commit();
            }
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisibleItems;
                int[] firstVisibleItems = null;

                // 스크롤시 로딩 구현예정 ( 이미지 많이 등록 후 실험 )
                if(!isLoading){
                    firstVisibleItems = layoutManager.findFirstVisibleItemPositions(firstVisibleItems);
                    if(firstVisibleItems != null && firstVisibleItems.length > 0)
                        pastVisibleItems = firstVisibleItems[0];
                }
            }
        });
    }

    public int getPage(int flag){
        if(flag == 0){
            uploadpage ++;
            return uploadpage;
        }
        else{
            likepage++;
            return likepage;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (userChoosenTask.equals("사진 가져오기"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data, SELECT_FILE);
            }
        }

    }
    private void onSelectFromGalleryResult(Intent data, int imagetype) {

        Bitmap bm = null;
        imageUri = data.getData();
        if (Build.VERSION.SDK_INT < 11) {
            imagePath = RealPathUtil.getRealPathFromURI_BelowAPI11(getContext(), imageUri);

        } else if (Build.VERSION.SDK_INT < 19) {

            imagePath = RealPathUtil.getRealPathFromURI_API11to18(getContext(), imageUri);
        } else {
            imagePath = RealPathUtil.getRealPathFromURI_API19(getContext(), imageUri);
        }


        try {
            in = getActivity().getContentResolver().openInputStream(data.getData());
            bm = BitmapFactory.decodeStream(in);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (imagetype == SELECT_FILE) {
            f = new File(imagePath);
            stream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.PNG, 100, stream);

            Glide.with(getContext())
                    .load(stream.toByteArray())
                    //   .placeholder(R.drawable.meme2)
                    .into(profImage);
            logoutDialog=new LogoutDialog(3,getContext(),mpositivelistener, mnewlistener);
            logoutDialog.setCancelable(true);
            logoutDialog.getWindow().setGravity(Gravity.CENTER);
            logoutDialog.show();
            //  imageview.setImageBitmap(bm);
        }

    }

    public View.OnClickListener mnewlistener= new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            logoutDialog.dismiss();
            Glide.with(getContext()).load(profileImage).error(R.drawable.logo_big2).into(profImage);


        }
    };

    public void uploadImg(){
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getContext(),
                "ap-northeast-2:e0ea2090-8c8e-4b43-a765-348b4fb30098", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2 // 리전
        );
        s3 = new AmazonS3Client(credentialsProvider);
        transferUtility = new TransferUtility(s3, getContext());
        s3.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
        s3.setEndpoint("s3.ap-northeast-2.amazonaws.com");

        transferUtility = new TransferUtility(s3, getContext());

        TransferObserver observer = transferUtility.upload("fofuploadtest",gettoken.checkIdx(getContext()).toString()+"prof"+f.getName(),f);

    }

}