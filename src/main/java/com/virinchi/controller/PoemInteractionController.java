package com.virinchi.controller;

import com.virinchi.model.Poem;
import com.virinchi.model.PoemComment;
import com.virinchi.model.PoemLike;
import com.virinchi.model.UserClass;
import com.virinchi.repository.PoemCommentRepository;
import com.virinchi.repository.PoemLikeRepository;
import com.virinchi.repository.PoemRepository;
import com.virinchi.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/poems")
public class PoemInteractionController {

    @Autowired private UserRepository uRepo;
    @Autowired private PoemRepository poemRepo;
    @Autowired private PoemLikeRepository likeRepo;
    @Autowired private PoemCommentRepository commentRepo;

    private UserClass requireUser(HttpSession session) { 
        String email = (String) session.getAttribute("activeUser");
        return email == null ? null : uRepo.findTopByEmailOrderByIdDesc(email);
    }

    @GetMapping("/{id}/meta")
    @ResponseBody
    public Map<String,Object> getMeta(@PathVariable("id") Long id, HttpSession session) {
        Map<String,Object> res = new HashMap<>();
        long likes = likeRepo.countByPoemId(id);
        long comments = commentRepo.countByPoemId(id);
        res.put("likes", likes);
        res.put("comments", comments);
        UserClass user = requireUser(session);
        boolean liked = false;
        if (user != null) {
            liked = likeRepo.findByPoemIdAndUserId(id, Long.valueOf(user.getId())).isPresent();
        }
        res.put("liked", liked);
        return res;
    }

    @PostMapping("/{id}/like")
    @ResponseBody
    public ResponseEntity<?> like(@PathVariable("id") Long id, HttpSession session) {
        UserClass user = requireUser(session);
        if (user == null) return ResponseEntity.status(401).body("LOGIN_REQUIRED");
        likeRepo.findByPoemIdAndUserId(id, Long.valueOf(user.getId())).orElseGet(() -> likeRepo.save(new PoemLike(id, Long.valueOf(user.getId()))));
        Map<String,Object> res = new HashMap<>();
        res.put("liked", true);
        res.put("likes", likeRepo.countByPoemId(id));
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/{id}/like")
    @ResponseBody
    public ResponseEntity<?> unlike(@PathVariable("id") Long id, HttpSession session) {
        UserClass user = requireUser(session);
        if (user == null) return ResponseEntity.status(401).body("LOGIN_REQUIRED");
        likeRepo.deleteByPoemIdAndUserId(id, Long.valueOf(user.getId()));
        Map<String,Object> res = new HashMap<>();
        res.put("liked", false);
        res.put("likes", likeRepo.countByPoemId(id));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}/comments")
    @ResponseBody
    public List<PoemComment> listComments(@PathVariable("id") Long id) {
        return commentRepo.findTop20ByPoemIdOrderByCreatedAtDesc(id);
    }

    @PostMapping("/{id}/comments")
    @ResponseBody
    public ResponseEntity<?> addComment(@PathVariable("id") Long id, @RequestParam("content") String content, HttpSession session) {
        UserClass user = requireUser(session);
        if (user == null) return ResponseEntity.status(401).body("LOGIN_REQUIRED");
        if (content == null || content.trim().isEmpty()) return ResponseEntity.badRequest().body("EMPTY_CONTENT");
        Poem poem = poemRepo.findById(id).orElse(null);
        if (poem == null) return ResponseEntity.notFound().build();
        PoemComment c = new PoemComment(id, Long.valueOf(user.getId()), user.getFirstName() + (user.getLastName()!=null?" "+user.getLastName():""), content.trim());
        commentRepo.save(c);
        Map<String,Object> res = new HashMap<>();
        res.put("id", c.getId());
        res.put("authorName", c.getAuthorName());
        res.put("content", c.getContent());
        res.put("createdAt", c.getCreatedAt());
        res.put("comments", commentRepo.countByPoemId(id));
        return ResponseEntity.ok(res);
    }
}

