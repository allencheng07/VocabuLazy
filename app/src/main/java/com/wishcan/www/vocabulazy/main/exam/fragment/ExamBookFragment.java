package com.wishcan.www.vocabulazy.main.exam.fragment;


import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wishcan.www.vocabulazy.R;
import com.wishcan.www.vocabulazy.VLApplication;
import com.wishcan.www.vocabulazy.main.MainActivity;
import com.wishcan.www.vocabulazy.main.exam.model.ExamModel;
import com.wishcan.www.vocabulazy.main.exam.view.ExamBookView;
import com.wishcan.www.vocabulazy.storage.databaseObjects.Book;
import com.wishcan.www.vocabulazy.storage.Database;
import com.wishcan.www.vocabulazy.widget.BookView;
import com.wishcan.www.vocabulazy.widget.ErrorView;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExamBookFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExamBookFragment extends ExamBaseFragment {

    private static final int TITLE_RES_ID = R.string.fragment_exam_book_title;

    public static ExamBookFragment newInstance() {
        ExamBookFragment fragment = new ExamBookFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mExamModel == null)
            mExamModel = new ExamModel((VLApplication) getActivity().getApplication());
        ((MainActivity)getActivity()).switchActionBarStr(MainActivity.FRAGMENT_FLOW.GO, "單元測驗");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ExamBookView examBookView = new ExamBookView(getActivity());
        ArrayList<Book> books = mExamModel.getBooks();
        LinkedList<String> bookNames = new LinkedList<>();
        examBookView.setOnBookItemClickListener(new BookView.OnBookItemClickListener() {
            @Override
            public void onBookItemClick(int position) {
                goExamLessonFragment(position);
            }

            @Override
            public void onNewItemClick() {
                //TODO: The function for future use
            }
        });

        if(books != null)
            for(int i = 0; i < books.size();i++)
                bookNames.add(books.get(i).getTitle());
        else
            return new ErrorView(getActivity()).setErrorMsg("get book failed");


        examBookView.refreshView(bookNames.size(), bookNames);
        return examBookView;
    }

    private void goExamLessonFragment(int bookIndex){
        Bundle args = new Bundle();
        args.putInt(ExamLessonFragment.BOOK_INDEX_STR, bookIndex);
        ((MainActivity) getActivity()).goFragment(ExamLessonFragment.class, args, "ExamLessonFragment", "ExamBookFragment");
    }
}
