package constant;

public class ZRecyclerViewFramgnet {
    public static final String str =
            "public class ${name}Fragment extends BaseFrameFrag {\n" +
                    "    private ZRecyclerView    recyclerView;\n" +
                    "    private ZRecyclerAdapter recyclerViewAdapter;\n" +
                    "    private int      mPage      = 1;\n" +
                    "    private String[] requestTag = new String[1];\n" +
                    "\n" +
                    "    public static ${name}Fragment newInstance() {\n" +
                    "        Bundle args = new Bundle();\n" +
                    "        Z1RecyclerFragment fragment = new Z1RecyclerFragment();\n" +
                    "        fragment.setArguments(args);\n" +
                    "        return fragment;\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    protected int getRootViewLayId() {\n" +
                    "        return R.layout.fragment_${layout};\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    protected void lazyLoad(@Nullable Bundle savedInstanceState) {\n" +
                    "        recyclerView = getView(R.id.recycler_view);\n" +
                    "        //recyclerView.setEmptyView(mActivity, R.layout.view_pullrecycler_empty);\n" +
                    "        recyclerView.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener<ListItemData>() {\n" +
                    "            @Override\n" +
                    "            public void onItemClick(View covertView, int position, ListItemData data) {\n" +
                    "                //TODO 添加点击事件 \n" +
                    "            }\n" +
                    "        });\n" +
                    "        recyclerView.setOnPullLoadMoreListener(new ZRecyclerView.PullLoadMoreListener() {\n" +
                    "            @Override\n" +
                    "            public void onRefresh() {\n" +
                    "                mPage = 1;\n" +
                    "                requestData();\n" +
                    "            }\n" +
                    "\n" +
                    "            @Override\n" +
                    "            public void onLoadMore() {\n" +
                    "                mPage = mPage + 1;\n" +
                    "                requestData();\n" +
                    "            }\n" +
                    "        });\n" +
                    "        recyclerView.refreshWithPull();\n" +
                    "    }\n" +
                    "\n" +
                    "    private void requestData() {\n" +
                    "        HashMap<String, String> mapApply = new HashMap<>();\n" +
                    "        mapApply.put(\"page\", String.valueOf(mPage));\n" +
                    "        requestTag[0] = ZHttp.post(\"\", mapApply, new ZResponse<ZReply>(ZReply.class) {\n" +
                    "            @Override\n" +
                    "            public void onSuccess(Response response, ZReply resObj) {\n" +
                    "                setDataToRecyclerView(resObj.data, mPage == 1);\n" +
                    "                if (isNoMore(resObj)) {\n" +
                    "                    recyclerView.setNoMore(true, 10, recyclerViewAdapter.getDatas());\n" +
                    "                } else {\n" +
                    "                    mPage++;\n" +
                    "                }\n" +
                    "            }\n" +
                    "\n" +
                    "            @Override\n" +
                    "            public void onError(int code, String error) {\n" +
                    "                super.onError(code, error);\n" +
                    "                setDataToRecyclerView(null, mPage == 1);\n" +
                    "            }\n" +
                    "\n" +
                    "            @Override\n" +
                    "            public void onFinished() {\n" +
                    "                super.onFinished();\n" +
                    "                recyclerView.setPullLoadMoreCompleted();\n" +
                    "            }\n" +
                    "        });\n" +
                    "    }\n" +
                    "\n" +
                    "    /**\n" +
                    "     * 分页是否有下一页\n" +
                    "     */\n" +
                    "    private boolean isNoMore(ZReply resobj) {\n" +
                    "        //TODO 判断是否有下一页\n" +
                    "        return false;\n" +
                    "    }\n" +
                    "\n" +
                    "    private void setDataToRecyclerView(ArrayList<ListItemData> list, boolean isClear) {\n" +
                    "        if (recyclerViewAdapter == null) {\n" +
                    "            recyclerViewAdapter = new ZRecyclerAdapter();\n" +
                    "            recyclerView.setAdapter(recyclerViewAdapter);\n" +
                    "        }\n" +
                    "\n" +
                    "        if (isClear) {\n" +
                    "            recyclerViewAdapter.setDatas(list);\n" +
                    "        } else {\n" +
                    "            recyclerViewAdapter.addDatas(list);\n" +
                    "        }\n" +
                    "    }\n" +
                    "}\n";
}