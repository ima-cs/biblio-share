package com.biblioshare.pret.dto;


import com.biblioshare.pret.dto.RemoteData.*;
import com.biblioshare.pret.entity.*;
import java.util.List;

public record LoanDetails(Loan loan, Copy copy, Book book, Member member, List<Fine> fines,
                          List<String> donneesManquantes) {}
