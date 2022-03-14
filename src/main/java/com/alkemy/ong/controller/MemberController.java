package com.alkemy.ong.controller;

import com.alkemy.ong.domain.Member;
import com.alkemy.ong.dto.ErrorDTO;
import com.alkemy.ong.dto.MemberApiResponse;
import com.alkemy.ong.dto.MemberCreationDTO;
import com.alkemy.ong.dto.MemberDTO;
import com.alkemy.ong.dto.MemberUpdateDTO;
import com.alkemy.ong.exception.MemberNotFoundException;
import com.alkemy.ong.mapper.MemberMapper;
import com.alkemy.ong.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping()
    public ResponseEntity<MemberApiResponse> getAll(@RequestParam(defaultValue = "0") Integer page) {
        MemberApiResponse response = new MemberApiResponse();
        String currentContextPath = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
        var members = memberService.getAll(page);
        if (members.hasPrevious()) {
            response.setPreviousPageUrl(currentContextPath.concat(String.format("/members?page=%d", page - 1)));
        }
        if (members.hasNext()) {
            response.setNextPageUrl(currentContextPath.concat(String.format("/members?page=%d", page + 1)));
        }
        response.setMembers(members.getContent().stream().map(MemberMapper::mapDomainToDTO).collect(Collectors.toList()));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberDTO> updateMember(@PathVariable Long id, @RequestBody MemberUpdateDTO memberUpdateDTO) throws MemberNotFoundException {
        Member member = MemberMapper.mapUpdateDTOToDomain(memberUpdateDTO);
        return ResponseEntity.ok(MemberMapper.mapDomainToDTO(memberService.updateMember(id, member)));
    }

    @PostMapping
    public ResponseEntity<MemberDTO> createMember(@Valid @RequestBody MemberCreationDTO memberCreationDTO) {
        Member member = MemberMapper.mapCreationDTOToDomain(memberCreationDTO);
        MemberDTO memberDTO = MemberMapper.mapDomainToDTO(memberService.createMember(member));
        return ResponseEntity.status(HttpStatus.CREATED).body(memberDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) throws MemberNotFoundException {
        memberService.deleteMember(id);
        return new ResponseEntity(HttpStatus.OK);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorDTO> handleMemberNotFoundExceptions(MemberNotFoundException ex) {
        ErrorDTO memberNotFound = ErrorDTO.builder()
                .code(HttpStatus.NOT_FOUND)
                .message(ex.getMessage()).build();
        return new ResponseEntity(memberNotFound, HttpStatus.NOT_FOUND);
    }

}
