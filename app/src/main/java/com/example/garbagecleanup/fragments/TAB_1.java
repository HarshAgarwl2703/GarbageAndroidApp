package com.example.garbagecleanup.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.JsonArrayRequest;
import com.android.volley.request.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.garbagecleanup.PrefManager;
import com.example.garbagecleanup.R;
import com.example.garbagecleanup.adapter.FeedRecyclerAdapter;
import com.example.garbagecleanup.helper.AppConstants;
import com.example.garbagecleanup.model.Issue_Model_Class;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TAB_1 extends Fragment {

    private static final String TAG = "TAB_1";
    RecyclerView recyclerView;
    FeedRecyclerAdapter feedRecyclerAdapter;
    List<Issue_Model_Class> IssueList;
    SwipeRefreshLayout swipeRefreshLayout;

    public static TAB_1 newInstance() {
        TAB_1 fragment = new TAB_1();
        return fragment;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tab_1, container, false);
        IssueList = new ArrayList<Issue_Model_Class>();
        recyclerView = view.findViewById(R.id.RecyclerTab1);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearVertical = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearVertical);

        feedRecyclerAdapter = new FeedRecyclerAdapter(getContext(), IssueList);
        recyclerView.setAdapter(feedRecyclerAdapter);

        swipeRefreshLayout = view.findViewById(R.id.SwipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                getSampleData();
                getData();
//                swipeRefreshLayout.setRefreshing(false);

            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getData();
    }

    //
    private void getData() {
        IssueList.clear();
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        Log.e(TAG, "getData: " + "in getdata");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, AppConstants.GET_POSTS, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject jsonObject = response.getJSONObject(i);
                        Issue_Model_Class issueModelClass = new Gson().fromJson(jsonObject.toString(), Issue_Model_Class.class);
//                        issueModelClass.setId(jsonObject.getInt("id"));
//                        issueModelClass.setImageUrl(jsonObject.getString("image"));
//                        issueModelClass.setCheckLiked(false);
//                        issueModelClass.setTitle(jsonObject.getString("title"));
//                        issueModelClass.setVotes(jsonObject.getInt("vote_count"));
//                        issueModelClass.setDescription(jsonObject.getString("Description"));
//                        issueModelClass.setLatitude(jsonObject.getString("latitude"));
//                        issueModelClass.setLongitude(jsonObject.getString("longitude"));
                        swipeRefreshLayout.setRefreshing(false);
                        IssueList.add(issueModelClass);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                    }
                }

                String Url = String.format(AppConstants.UPVOTE_LIST, PrefManager.getUser().getUserId());
                final ArrayList<Integer> postArrayList = new ArrayList<>();
                StringRequest stringRequest = new StringRequest(Request.Method.GET, Url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse: " + response.toString());
                        try {
                            JSONArray t = new JSONArray(response);
                            for (int i = 0; i < t.length(); i++) {
                                JSONObject object = t.getJSONObject(i);
                                postArrayList.add(object.getInt("post_id"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        for (Issue_Model_Class IssueObject : IssueList) {
                            for (int i : postArrayList) {
                                if (IssueObject.getId() == i) {
                                    IssueObject.setCheckLiked(true);
                                }
                            }
                        }

                        Log.e(TAG, "onResponse: " + IssueList);
                        feedRecyclerAdapter.notifyDataSetChanged();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse: " + error.toString());
                    }
                });
                RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                stringRequest.setShouldCache(false);
                requestQueue.add(stringRequest);
                feedRecyclerAdapter.notifyDataSetChanged();

                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());
                progressDialog.dismiss();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        jsonArrayRequest.setShouldCache(false);
        requestQueue.add(jsonArrayRequest);
    }


}
