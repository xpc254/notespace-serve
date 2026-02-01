package com.notespace.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.notespace.entity.Notebook;

import java.util.List;

public interface NotebookService extends IService<Notebook> {

    List<Notebook> getNotebookList(Long userId);

    Notebook createNotebook(Long userId, String name, String icon, String color);

    void updateNotebook(Long userId, Long id, String name, String icon, String color);

    void deleteNotebook(Long userId, Long id);
}
