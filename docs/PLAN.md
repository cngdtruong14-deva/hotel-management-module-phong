# Kế hoạch GitHub — 2026-10-21

Spec: specs/2026-10-21-github.md. Source chuẩn: ../ModulePhong-current. Hồ sơ: project/PHAN_CHIA_MODULE.md và NHAT_KY_CHINH_SUA.md. Context: not measured.

| ID | Phụ thuộc | Files | Chấp nhận / kiểm chứng | LOC dự kiến | Trạng thái |
|---|---|---|---|---|---|
| GH01 | Không | .gitignore, README.md, ModulePhong-current/{README,CONTRACT,CHANGELOG,VERSION}.md/json, docs/project/*, CURRENT_MODULE.md, handoff metadata/hash, bản sao docs, manifest/ZIP | Lọc đúng phạm vi, giữ source Java/SQL; test.ps1 và release VerifyOnly đạt | <=60 config; docs/generated excluded | done |
| GH02 | GH01; mặc định Private khi chưa có lựa chọn | Git index/commit, remote GitHub, docs/PLAN.md | gh repo view, git ls-remote cùng SHA; source và README có trên GitHub | 0 source | done |

Baseline GH01: audit/2026-10-21-github/baseline.json lưu hash trước sửa. Git root chưa có commit, không remote; tài khoản gh cngdtruong14-deva. Không đưa log audit, archive/work hay db.properties lên remote. Tối đa một task in_progress; thực thi tuần tự.

GH01 hoàn tất: test.ps1 đạt 5 suites; src 58 file và SQL 9 file khớp baseline. release -VerifyOnly và ZIP/hash đạt; 135 file staged ~4.17 MB. Không phát hiện secret theo các mẫu đã quét; MAT_KHAU_CUA_BAN là placeholder. .gitignore mới 17 dòng (baseline cũ 4 dòng: 21 added+deleted); metadata JSON 2 dòng thay (4 added+deleted); tổng manual config 25 LOC, docs/generated excluded. Các lần quét ban đầu báo nhầm placeholder và xuống dòng, đã chỉnh bộ quét local; không sửa mã ứng dụng. Bộ SQL vẫn 2026.10.11.3; bản đóng gói source 2026.10.21.1.
GH02: user đã cho phép đưa source lên GitHub; chưa chọn visibility sau câu hỏi. Dùng mặc định Private để hoàn tất upload, không tự mời người khác hoặc công khai. Thành viên cần được mời bằng GitHub username, hoặc owner chọn Public sau. Repository dự kiến cngdtruong14-deva/hotel-management-module-phong.

GH02 hoàn tất upload: https://github.com/cngdtruong14-deva/hotel-management-module-phong ; Private; nhánh mặc định main. Commit source 69bac113ea44149a3cfa227c59ce309153550007 đã xác minh bằng git ls-remote; GitHub Contents API đọc được README và VERSION. Remote origin cấu hình HTTPS. Không tạo PR vì đây là lần xuất bản đầu, chưa có nhánh nền khác.
Quyền xem thành viên: chưa cấp vì chưa có GitHub usernames và chưa được chọn Public. Source đã upload; để chia sẻ cần người dùng chọn Public hoặc cung cấp tài khoản cần mời. Không coi việc upload Private là đã hoàn tất cấp quyền nhóm.
Manual source/test LOC GH02: 0; chỉ docs/Git metadata. Các tệp docs ~120 dòng và manifest/ZIP là tài liệu/generated, không tính budget manual code. Context: not measured. Không đổi cấu hình model/security. File AGENTS.md local giữ nguyên và không commit vì chứa chỉ dẫn môi trường cá nhân.

Người dùng đã chọn Public qua câu trả lời ngày 2026-10-21. Đã chuyển visibility thành PUBLIC bằng gh repo edit; gh repo view và GitHub REST API không xác thực đều xác nhận truy cập công khai. Thành viên có thể mở link xem source mà không cần được mời. Yêu cầu cấp quyền xem nhóm đã hoàn tất.
