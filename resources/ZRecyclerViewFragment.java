package ${package};

public class ${name}Fragment extends BaseFrameFrag {
    private ZRecyclerView    recyclerView;
    private ZRecyclerAdapter recyclerViewAdapter;
    private int      mPage      = 1;
    private String[] requestTag = new String[1];

    public static ${name}Fragment newInstance() {
        Bundle args = new Bundle();
        Z1RecyclerFragment fragment = new Z1RecyclerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getRootViewLayId() {
        return R.layout.fragment_superrecycler;
    }

    @Override
    protected void lazyLoad(@Nullable Bundle savedInstanceState) {
        recyclerView = getView(R.id.recycler_view);
        //recyclerView.setEmptyView(mActivity, R.layout.view_pullrecycler_empty);
        recyclerView.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener<String>() {
            @Override
            public void onItemClick(View covertView, int position, String data) {
                //TODO 添加点击事件 
            }
        });
        recyclerView.setOnPullLoadMoreListener(new ZRecyclerView.PullLoadMoreListener() {
            @Override
            public void onRefresh() {
                mPage = 1;
                requestData();
            }

            @Override
            public void onLoadMore() {
                mPage = mPage + 1;
                requestData();
            }
        });
        recyclerView.refreshWithPull();
    }

    private void requestData() {
        HashMap<String, String> mapApply = new HashMap<>();
        mapApply.put("page", String.valueOf(mPage));
        requestTag[0] = ZHttp.post("", mapApply, new ZResponse<ZReply>(ZReply.class) {
            @Override
            public void onSuccess(Response response, ZReply resObj) {
                setDataToRecyclerView(resObj.data, mPage == 1);
                if (isNoMore(resObj)) {
                    recyclerView.setNoMore(true, 10, recyclerViewAdapter.getDatas());
                } else {
                    mPage++;
                }
            }

            @Override
            public void onError(int code, String error) {
                super.onError(code, error);
                setDataToRecyclerView(null, mPage == 1);
            }

            @Override
            public void onFinished() {
                super.onFinished();
                recyclerView.setPullLoadMoreCompleted();
            }
        });
    }

    /**
     * 分页是否有下一页
     */
    private boolean isNoMore(ZReply resobj) {
        //TODO 判断是否有下一页
        return false;
    }

    private void setDataToRecyclerView(ArrayList<ListItemData> list, boolean isClear) {
        if (recyclerViewAdapter == null) {
            recyclerViewAdapter = new ZRecyclerAdapter();
            recyclerView.setAdapter(recyclerViewAdapter);
        }

        if (isClear) {
            recyclerViewAdapter.setDatas(list);
        } else {
            recyclerViewAdapter.addDatas(list);
        }
    }
}
