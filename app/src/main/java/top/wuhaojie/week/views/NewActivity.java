package top.wuhaojie.week.views;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import top.wuhaojie.week.R;
import top.wuhaojie.week.constant.Constants;
import top.wuhaojie.week.data.ImageFactory;
import top.wuhaojie.week.entities.TaskDetailEntity;
import top.wuhaojie.week.entities.TaskState;
import top.wuhaojie.week.utils.SnackBarUtils;

public class NewActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.cl)
    CoordinatorLayout mCl;
    @BindView(R.id.et_title)
    EditText mEtTitle;
    @BindView(R.id.et_content)
    EditText mEtContent;
    @BindView(R.id.sdv_bg)
    SimpleDraweeView mSdvBg;
    @BindView(R.id.tv_date)
    TextView mTvDate;
    private final List<String> mBgImgs = ImageFactory.createBgImgs();
    private String mCurrBgUri;

    private interface IState {
        void initView(Intent intent, Bundle savedInstanceState);
    }

    private class CreateNew implements IState {

        @Override
        public void initView(Intent intent, Bundle savedInstanceState) {
            mEtContent.requestFocus();
            int i = new Random(System.currentTimeMillis()).nextInt(mBgImgs.size());
            loadBgImgWithIndex(i);
            String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            mTvDate.setText(date);
        }
    }

    private class EditOld implements IState {

        @Override
        public void initView(Intent intent, Bundle savedInstanceState) {

            mEtTitle.setFocusable(false);
            mEtTitle.setOnTouchListener((v, event) -> {
                mEtTitle.setFocusableInTouchMode(true);
                return false;
            });

            mEtContent.setFocusable(false);
            mEtContent.setOnTouchListener((v, event) -> {
                mEtContent.setFocusableInTouchMode(true);
                return false;
            });

            TaskDetailEntity entity = (TaskDetailEntity) intent.getSerializableExtra(Constants.INTENT_EXTRA_EDIT_TASK_DETAIL_ENTITY);
            intent.putExtra(Constants.INTENT_EXTRA_DAY_OF_WEEK, entity.getDayOfWeek());
            mEtTitle.setText(entity.getTitle());
            mEtContent.setText(entity.getContent());
            loadBgImgWithUri(entity.getIcon());
            String date = new SimpleDateFormat("yyyy/MM/dd").format(new Date(entity.getTimeStamp()));
            mTvDate.setText(date);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        int mode = intent.getIntExtra(Constants.INTENT_EXTRA_MODE_OF_NEW_ACT, Constants.MODE_OF_NEW_ACT.MODE_CREATE);
        IState state;
        if (mode == Constants.MODE_OF_NEW_ACT.MODE_EDIT)
            state = new EditOld();
        else
            state = new CreateNew();
        state.initView(intent, savedInstanceState);


        View decorView = this.getWindow().getDecorView();
//            View decorView = mToolbar;
        decorView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    int x = (int) (mFab.getWidth() / 2 + mFab.getX());
                    int y = (int) (mFab.getHeight() / 2 + mFab.getY());
                    Animator animator = ViewAnimationUtils.createCircularReveal(decorView, x, y, 0, decorView.getHeight());
                    animator.setDuration(400);
                    animator.start();
                }
            }
        });


    }


    public void loadBgImgWithIndex(int i) {
        loadBgImgWithUri(mBgImgs.get(i));
    }

    public void loadBgImgWithUri(String uri) {
        mCurrBgUri = uri;
        mSdvBg.setImageURI(uri);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_choose_icon:
                showIconChooseDialog();
                break;
        }
        return true;
    }

    private void showIconChooseDialog() {

        ChoosePaperColorDialog.newInstance(mCurrBgUri).show(getSupportFragmentManager(), "IconChooseDialog");


    }

    @OnClick(R.id.fab)
    public void onClick(View v) {
        String title = mEtTitle.getText().toString().trim();
        String content = mEtContent.getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            SnackBarUtils.show(mCl, "请填写内容后保存哦~");
            return;
        }
        if (TextUtils.isEmpty(title)) {
            title = content.substring(0, Math.min(5, content.length()));
        }

        int dayOfWeek = getIntent().getIntExtra(Constants.INTENT_EXTRA_DAY_OF_WEEK, Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        TaskDetailEntity taskDetailEntity = new TaskDetailEntity(dayOfWeek);
        taskDetailEntity.setTitle(title);
        taskDetailEntity.setContent(content);
        taskDetailEntity.setState(TaskState.DEFAULT);
        taskDetailEntity.setTimeStamp(System.currentTimeMillis());
        taskDetailEntity.setIcon(mCurrBgUri);

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.INTENT_BUNDLE_NEW_TASK_DETAIL, taskDetailEntity);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }


}
