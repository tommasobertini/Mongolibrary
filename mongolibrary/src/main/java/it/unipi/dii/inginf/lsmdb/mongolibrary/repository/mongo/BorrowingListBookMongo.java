package it.unipi.dii.inginf.lsmdb.mongolibrary.repository.mongo;

import it.unipi.dii.inginf.lsmdb.mongolibrary.util.DateConverter;
import org.bson.Document;

import java.time.LocalDate;

public class BorrowingListBookMongo {

    private String bookTitle;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    private String status;

    public BorrowingListBookMongo(String bookTitle, LocalDate borrowDate, LocalDate returnDate, String status)
    {
        this.bookTitle = bookTitle;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    public BorrowingListBookMongo(Document borrowedBook)
    {
        this(
                borrowedBook.getString("booktitle"),
                DateConverter.toLocalDate(borrowedBook.getInteger("borrowdate")),
                DateConverter.toLocalDate(borrowedBook.getInteger("returndate")),
                borrowedBook.getString("status")
        );
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String fullDisplay()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Book: ").append(bookTitle).append(System.getProperty("line.separator"))
                .append("Borrow date: ").append(borrowDate).append(System.getProperty("line.separator"))
                .append("Return date: ").append(returnDate).append(System.getProperty("line.separator"))
                .append("Status: ").append(status).append(System.getProperty("line.separator"));
        return sb.toString();
    }
}
