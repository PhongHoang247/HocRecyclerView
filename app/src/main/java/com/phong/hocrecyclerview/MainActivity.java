package com.phong.hocrecyclerview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.phong.model.Contact;
import com.phong.model.OnLoadMoreListerner;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<Contact> contacts;
    ContactAdapter contactAdapter;
    Random random = new Random();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addControls();
        addEvents();
    }

    private void addEvents() {
        //Xử lý nạp dữ liệu:
        contactAdapter.setOnLoadMoreListerner(new OnLoadMoreListerner() {
            @Override
            public void OnLoadMore() {
                contacts.add(null);
                //notifyItemInserted: Yêu cầu vẽ lại từ phần tử bắt đầu từ vị trí Insert
                contactAdapter.notifyItemInserted(contacts.size()-1);//Chèn biểu tượng cuối cùng vào
                new Handler().postDelayed(new Runnable() {//Trì hoãn lại thời gian nào đó
                    @Override
                    public void run() {
                        contacts.remove(contacts.size()-1);//Xóa cái null
                        //Tiến hành bắt vẽ lại
                        contactAdapter.notifyItemRemoved(contacts.size());//Xóa ngay vị trí đó đi rồi vẽ lại
                        //Tiến hành vẽ dữ liệu: Mỗi lần xoay tải bn phần tử
                        int index = contacts.size();
                        int end = index + 20;//Giả sử đang load 30 phần tử rồi tải nữa + 20 = 50
                        //Gọi trên Server:
                        for (int i = index; i < end; i++)//Bắ́t đầu từ vị trí cuối cùng index cho tới vị trí mới end để vẽ lên RecycleView
                        {
                            //Tạo giả dữ liệu:
                            Contact c = new Contact();
                            c.setName("Tên" + i);
                            String phone = "098";
                            //Lấy ngẫu nhiên đầu số 098:
                            int x = random.nextInt(3);
                            if (x == 1){
                                phone = "090";
                            }
                            else if (x == 2)
                            {
                                phone = "094";
                            }
                            for (int p = 0; p < 7; p++)
                            {
                                phone +=random.nextInt(10);
                            }
                            c.setPhone(phone);
                            contacts.add(c);
                        }
                        //Ra lệch cho nó vẽ lại:
                        contactAdapter.notifyDataSetChanged();
                        contactAdapter.isLoading = false;//Loading = false là xong r còn true là đang tải...
                    }
                },5000);//Số giây muốn chờ
            }
        });
    }

    private void addControls() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        contacts = new ArrayList<>();
        contactAdapter = new ContactAdapter();
        recyclerView.setAdapter(contactAdapter);

        //Nạp dữ liệu giả: lấy ds contacts
        for (int i = 0; i < 30; i++)
        {
            Contact c = new Contact();
            c.setName("Tên" + i);
            String phone = "098";
            //Lấy ngẫu nhiên đầu số 098:
            int x = random.nextInt(3);
            if (x == 1){
                phone = "090";
            }
            else if (x == 2)
            {
                phone = "094";
            }
            for (int p = 0; p < 7; p++)
            {
                phone +=random.nextInt(10);
            }
            c.setPhone(phone);
            contacts.add(c);
        }
    }

    //Làm ContactAdapter: làm cho lớp nào thì lấy lớp đó .Adapter
    class ContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    {
        private final int VIEW_TYPE_ITEM = 0;//Tạo biến để đánh dấu đang ở tình trạng nào loading/show
        private final int VIEW_TYPE_LOADING = 1;
        //Khai báo 1 Interface:
        OnLoadMoreListerner onLoadMoreListerner;

        public OnLoadMoreListerner getOnLoadMoreListerner() {
            return onLoadMoreListerner;
        }

        public void setOnLoadMoreListerner(OnLoadMoreListerner onLoadMoreListerner) {
            this.onLoadMoreListerner = onLoadMoreListerner;
        }

        //Kiểm tra xem nó có đang tải hay không?:
        public boolean isLoading = false;
        int visibleThreadhold = 5;//Quy định phần tử nhìn thấy
        //Vì có phân trang nên chúng ta cần biết cái dòng nào đc nhìn thấy
        int lastVisibleItem;
        int totalItemCount;
        //Bổ sung thêm Constructors để khởi tạo các biến trên:
        public ContactAdapter()
        {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();//lấy đối tượng đó ra
            //Gán sự kiện Scroll cho recyclerView:
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    //Lấy tổng của các phần tử:
                    totalItemCount = linearLayoutManager.getItemCount();
                    /*
                    getCount lấy LayoutManager ra thì sẽ đếm đc trong đó có bao nhiêu phần tử (bao nhiêu Holder trong đó)
                    Tự động phát sinh final vì linearLayoutManager là biến Local
                    AnomousListerner mà muốn truy suất biến Local thì phải khai báo Final
                     */
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    //Google hỗ trợ sẵn, tìm đc phần tử cuối cùng tức là lấy đc cái VisibleItemPosition cuối cùng để ProgressBar xoay
                    //Kiểm tra xem có tải tiếp ko
                    if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreadhold))
                    {
                        if (onLoadMoreListerner != null)
                        {
                            onLoadMoreListerner.OnLoadMore();
                        }
                        //Đánh dấu: chưa loading thì cho loading
                        isLoading = true;
                    }
                }
            });
        }
        //Hàm để tạo Holder: Vẽ từng dòng contacts
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_ITEM)
            {
                View contactView = LayoutInflater.from(MainActivity.this).inflate(R.layout.contact_item, parent, false);
                return new ContactViewHolder(contactView);
            }
            if (viewType == VIEW_TYPE_LOADING)
            {
                View loadingView = LayoutInflater.from(MainActivity.this).inflate(R.layout.loading_item, parent, false);
                return new LoadingViewHolder(loadingView);
            }
            return null;
        }
        //Nạp dữ liệu cho Holder: nạp dữ liệu lên giao diện cho từng dòng
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ContactViewHolder)
            {
                //Lấy thông tin contacts tại vị trí:
                Contact contact = contacts.get(position);
                //Có contact r mới lấy holder ra:
                ContactViewHolder contactViewHolder = (ContactViewHolder) holder;
                //Có ContactHolder r có thể truy suất trực tiếp 2 biến đối tượng txtName, txtPhone
                contactViewHolder.txtName.setText(contact.getName());
                contactViewHolder.txtPhone.setText(contact.getPhone());
            }
            else if (holder instanceof LoadingViewHolder)
            {
                //Lấy progressBar ra:
                LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                //Hiển thị lên:
                loadingViewHolder.progressBar.setIndeterminate(true);
            }
        }
        //Trả về số lượng phần tử của nó
        @Override
        public int getItemCount() {
            return contacts == null?0:contacts.size();//Trả về đúng số lượng phần tử đó
        }

        @Override
        public int getItemViewType(int position) {
            return contacts.get(position) == null?VIEW_TYPE_LOADING:VIEW_TYPE_ITEM;
        }
    }
    /*
    Mục đích của LoadingViewHolder này là show cái ProgressBar lên
    mà ProgressBar là 1 cái view nằm trong 1 cái view nào đó
    cụ thể ở đây là itemvView nên gọi findViewById để lấy progressBar
     */
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        //Khai báo:
        public ProgressBar progressBar;
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }
    }
    //Tạo 1 lớp để hiển thị chi tiết cho 1 Contact Item
    static class ContactViewHolder extends RecyclerView.ViewHolder
    {
        /*
        Lấy ra khởi tạo các controls trên giao diện contact_item.xml
         */
        public TextView txtName;
        public TextView txtPhone;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.txtName);
            txtPhone = (TextView) itemView.findViewById(R.id.txtPhone);
        }
    }
}
