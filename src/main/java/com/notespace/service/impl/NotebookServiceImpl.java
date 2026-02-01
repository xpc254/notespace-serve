package com.notespace.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.notespace.entity.Notebook;
import com.notespace.exception.BusinessException;
import com.notespace.mapper.NotebookMapper;
import com.notespace.mapper.NoteMapper;
import com.notespace.service.NotebookService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class NotebookServiceImpl extends ServiceImpl<NotebookMapper, Notebook> implements NotebookService {

    @Resource
    private NoteMapper noteMapper;

    @Override
    public List<Notebook> getNotebookList(Long userId) {
        LambdaQueryWrapper<Notebook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notebook::getUserId, userId);
        wrapper.orderByAsc(Notebook::getSortOrder);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public Notebook createNotebook(Long userId, String name, String icon, String color) {
        if (StrUtil.isBlank(name)) {
            throw new BusinessException("笔记本名称不能为空");
        }

        Notebook notebook = new Notebook();
        notebook.setUserId(userId);
        notebook.setName(name);
        notebook.setIcon(StrUtil.isBlank(icon) ? "folder" : icon);
        notebook.setColor(StrUtil.isBlank(color) ? "#3b82f6" : color);
        notebook.setSortOrder(0);

        baseMapper.insert(notebook);
        return notebook;
    }

    @Override
    public void updateNotebook(Long userId, Long id, String name, String icon, String color) {
        Notebook notebook = baseMapper.selectById(id);
        if (notebook == null) {
            throw new BusinessException("笔记本不存在");
        }
        if (!notebook.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此笔记本");
        }

        if (StrUtil.isNotBlank(name)) {
            notebook.setName(name);
        }
        if (StrUtil.isNotBlank(icon)) {
            notebook.setIcon(icon);
        }
        if (StrUtil.isNotBlank(color)) {
            notebook.setColor(color);
        }

        baseMapper.updateById(notebook);
    }

    @Override
    public void deleteNotebook(Long userId, Long id) {
        Notebook notebook = baseMapper.selectById(id);
        if (notebook == null) {
            throw new BusinessException("笔记本不存在");
        }
        if (!notebook.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此笔记本");
        }

        // 检查笔记本内是否有笔记
        Long count = noteMapper.selectCount(
                new LambdaQueryWrapper<com.notespace.entity.Note>()
                        .eq(com.notespace.entity.Note::getNotebookId, id)
        );
        if (count > 0) {
            throw new BusinessException("该笔记本内尚有笔记，无法删除");
        }

        baseMapper.deleteById(id);
    }
}
