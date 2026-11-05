package com.example.dzl.mapstruct;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dzl.model.dto.AddBlogDTO;
import com.example.dzl.model.dto.BlogDetailDTO;
import com.example.dzl.model.dto.UpdateBlogDTO;
import com.example.dzl.model.entity.Blog;
import com.example.dzl.model.entity.Category;
import com.example.dzl.model.entity.Tag;
import com.example.dzl.model.req.AddBlogReq;
import com.example.dzl.model.req.UpdateBlogReq;
import com.example.dzl.model.vo.BlogAdminVO;
import com.example.dzl.model.vo.BlogDetailVO;
import com.example.dzl.model.vo.CategoryVO;
import com.example.dzl.model.vo.TagVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BlogConvert {

    // 发布博客转换
    AddBlogDTO reqToDTO(AddBlogReq req);
    Blog dtoToEntity(AddBlogDTO dto);

    // 编辑博客转换
    UpdateBlogDTO reqToDTO(UpdateBlogReq req);
    Blog dtoToEntity(UpdateBlogDTO dto);

    // 博客详情转换
    @Mapping(target = "createTime", source = "createTime", dateFormat = "yyyy-MM-dd HH:mm:ss")
    BlogDetailDTO entityToDTO(Blog blog);



    BlogDetailVO dtoToVO(BlogDetailDTO dto);

    // 博客列表转换
    default List<BlogDetailDTO> entityListToDTOList(List<Blog> blogList){
        if ( blogList == null ) {
            return null;
        }
        return blogList.stream()
                .map(this::entityToDTOX)
                .toList();
    }
    @Mapping(target = "createTime", source = "createTime", dateFormat = "yyyy-MM-dd")
    BlogDetailDTO entityToDTOX(Blog blog);
    Page<BlogDetailVO> dtoPageToVOPage(Page<BlogDetailDTO> dtoList);


    // 分类转换
    CategoryVO categoryToVO(Category category);
    List<CategoryVO> categoryListToVOList(List<Category> categoryList);

    // 标签转换
    TagVO tagToVO(Tag tag);
    Page<TagVO> tagPageToVOPage(Page<Tag> tagPage);


    // 管理端博客列表转换
    BlogAdminVO entityToAdminVO(Blog blog);
    List<BlogAdminVO> entityListToAdminVOList(List<Blog> blogList);


}
