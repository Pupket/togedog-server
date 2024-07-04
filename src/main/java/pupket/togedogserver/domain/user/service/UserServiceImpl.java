package pupket.togedogserver.domain.user.service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pupket.togedogserver.domain.token.repository.SocialAccessTokenRepository;
import pupket.togedogserver.domain.user.constant.AccountStatus;
import pupket.togedogserver.domain.user.constant.Visibility;
import pupket.togedogserver.domain.user.dto.request.SignUpRequest;
import pupket.togedogserver.domain.user.dto.request.UpdateRequest;
import pupket.togedogserver.domain.user.dto.response.FindUserResponse;
import pupket.togedogserver.domain.user.entity.User;
import pupket.togedogserver.domain.user.mapper.UserMapper;
import pupket.togedogserver.domain.user.repository.UserRepository;
import pupket.togedogserver.global.auth.service.OAuth2RevokeService;
import pupket.togedogserver.global.exception.ExceptionCode;
import pupket.togedogserver.global.exception.customException.MemberException;
import pupket.togedogserver.global.jwt.entity.JwtToken;
import pupket.togedogserver.global.jwt.service.JwtService;
import pupket.togedogserver.global.jwt.util.JwtUtils;
import pupket.togedogserver.global.security.CustomUserDetail;
import pupket.togedogserver.global.security.util.PasswordUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final SocialAccessTokenRepository socialAccessTokenRepository;
    private final PasswordUtil passwordUtil;
    private final OAuth2RevokeService oAuth2RevokeService;

    public void create(CustomUserDetail userDetail, SignUpRequest request) {
        User user = getUserById(userDetail.getUuid());

        String password = passwordUtil.generateRandomPassword();
        password = passwordEncoder.encode(password);
        user.updateInfo(request);
        user.updatePassword(password);

        userRepository.save(user);
    }

    public void logout(String refreshToken, HttpServletResponse response) {
        jwtUtils.handleExpiredRefreshToken(refreshToken, response);
    }

    public JwtToken reissueToken(String refreshToken) {
        return jwtService.reissueTokenByRefreshToken(refreshToken);
    }


    private User getUserById(Long memberUuid) {
        return userRepository.findByUuid(memberUuid).
                orElseThrow(
                        () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
                );
    }

    public FindUserResponse getMemberDetails(Long memberId) {
        User user = getUserById(memberId);

        //TODO:: 사진 업로드 로직 필요(버킷 생성 후)
        return userMapper.of(user);
    }

    public void updateMember(CustomUserDetail userDetail, UpdateRequest updateReq) {
        User findMember = getUserById(userDetail.getUuid());
        validateExistingNickname(updateReq.getNickName());

        findMember.toBuilder()
                .nickname(updateReq.getNickName())
                .address1(updateReq.getAddress1())
                .address2(updateReq.getAddress2())
                .genderVisibility(Visibility.valueOf(updateReq.getGenderVisibility()))
                .build();

        findMember.updateInfo(updateReq);
        userRepository.save(findMember);
    }

    private void validateExistingNickname(String nickname) {
        userRepository.findByNickname(nickname).ifPresent(member -> {
            throw new MemberException(ExceptionCode.NICKNAME_ALREADY_EXISTS);
        });


    }

    public void deleteSocialMember(Long uuid) {
        User findUser = getUserById(uuid);

        socialAccessTokenRepository.findByUser(findUser).ifPresent(
                accessToken -> {
                    String socialAccessToken = accessToken.getSocialAccessToken();
                    revokeSocialAccessToken(findUser, socialAccessToken);
                    socialAccessTokenRepository.delete(accessToken);
                }
        );
        findUser.updateStatus();
    }

    private void revokeSocialAccessToken(User findUser, String socialAccessToken) {
        switch (findUser.getRole()) {
            case MEMBER_KAKAO -> oAuth2RevokeService.revokeKakao(socialAccessToken);
            case MEMBER_GOOGLE -> oAuth2RevokeService.revokeGoogle(socialAccessToken);
            case MEMBER_NAVER -> oAuth2RevokeService.revokeNaver(socialAccessToken);
        }
    }

    public void validateUser(String name) {
        User findUser = userRepository.findByEmail(name).orElseThrow(
                () -> new MemberException(ExceptionCode.NOT_FOUND_MEMBER)
        );
        if (!findUser.getAccountStatus().equals(AccountStatus.valueOf("ACTIVE"))) {
            throw new MemberException(ExceptionCode.MEMBER_ALREADY_WITHDRAW);
        }

    }
}