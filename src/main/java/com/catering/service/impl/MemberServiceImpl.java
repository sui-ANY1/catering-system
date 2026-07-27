package com.catering.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.entity.Member;
import com.catering.mapper.MemberMapper;
import com.catering.service.MemberService;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl
        extends ServiceImpl<MemberMapper, Member>
        implements MemberService {

}