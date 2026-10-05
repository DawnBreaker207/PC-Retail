export interface PolicySection {
  heading: string;
  body: string;
}

export interface PolicyContent {
  title: string;
  sections: PolicySection[];
}

export const POLICY_CONTENT: Record<string, PolicyContent> = {
  'bao-hanh': {
    title: 'Chính sách bảo hành',
    sections: [
      {
        heading: 'Thời gian bảo hành',
        body: 'Toàn bộ sản phẩm bán tại DAWN PC Retail được bảo hành chính hãng từ 12 đến 36 tháng tùy loại sản phẩm, thời hạn cụ thể được ghi trên phiếu bảo hành/hóa đơn mua hàng.',
      },
      {
        heading: 'Đổi mới trong 7 ngày (DOA)',
        body: 'Sản phẩm lỗi do nhà sản xuất phát sinh trong 7 ngày đầu tiên kể từ ngày mua được đổi mới 1-1, không tính vào thời gian bảo hành sửa chữa thông thường.',
      },
      {
        heading: 'Trường hợp không được bảo hành',
        body: 'Sản phẩm bị vào nước, cháy nổ do nguồn điện, va đập vật lý, tự ý tháo mở hoặc mất tem bảo hành sẽ không thuộc phạm vi bảo hành miễn phí.',
      },
    ],
  },
  'doi-tra': {
    title: 'Chính sách đổi trả',
    sections: [
      {
        heading: 'Điều kiện đổi trả',
        body: 'Sản phẩm còn nguyên hộp, đầy đủ phụ kiện, chưa qua sử dụng hoặc có lỗi do nhà sản xuất được đổi trả trong vòng 7 ngày kể từ ngày nhận hàng.',
      },
      {
        heading: 'Quy trình đổi trả',
        body: 'Khách hàng liên hệ hotline hoặc showroom gần nhất kèm hóa đơn mua hàng, nhân viên sẽ kiểm tra tình trạng sản phẩm và xử lý đổi trả trong vòng 24-48 giờ.',
      },
    ],
  },
  'van-chuyen': {
    title: 'Chính sách vận chuyển',
    sections: [
      {
        heading: 'Miễn phí giao hàng toàn quốc',
        body: 'Áp dụng cho đơn hàng từ 500.000đ trở lên trong nội thành, các khu vực khác tính phí theo bảng giá vận chuyển của đơn vị đối tác.',
      },
      {
        heading: 'Thời gian giao hàng',
        body: 'Nội thành: 1-2 ngày làm việc. Tỉnh thành khác: 2-5 ngày làm việc tùy khu vực. Khách hàng có thể chọn nhận tại showroom để nhận hàng ngay.',
      },
    ],
  },
  'bao-mat': {
    title: 'Chính sách bảo mật',
    sections: [
      {
        heading: 'Thu thập thông tin',
        body: 'DAWN PC Retail chỉ thu thập thông tin cần thiết để xử lý đơn hàng (họ tên, số điện thoại, địa chỉ giao hàng) và không chia sẻ cho bên thứ ba ngoài mục đích vận chuyển/thanh toán.',
      },
      {
        heading: 'Bảo mật thanh toán',
        body: 'Mọi giao dịch thanh toán trực tuyến được xử lý qua cổng thanh toán VNPay đạt chuẩn bảo mật PCI DSS, DAWN PC Retail không lưu trữ thông tin thẻ của khách hàng.',
      },
    ],
  },
  'thanh-toan': {
    title: 'Hướng dẫn thanh toán',
    sections: [
      {
        heading: 'Thanh toán khi nhận hàng (COD)',
        body: 'Áp dụng cho đơn hàng giao trong nội thành, khách hàng kiểm tra hàng và thanh toán trực tiếp cho nhân viên giao hàng.',
      },
      {
        heading: 'Thanh toán trực tuyến qua VNPay',
        body: 'Hỗ trợ thanh toán qua thẻ ATM nội địa, thẻ quốc tế Visa/Mastercard và ví điện tử liên kết VNPay, đơn hàng được xử lý ngay sau khi thanh toán thành công.',
      },
    ],
  },
  'huong-dan-mua-hang': {
    title: 'Hướng dẫn mua hàng',
    sections: [
      {
        heading: 'Bước 1 — Chọn sản phẩm',
        body: 'Tìm sản phẩm qua thanh tìm kiếm hoặc duyệt theo danh mục, xem chi tiết thông số kỹ thuật và đánh giá trước khi thêm vào giỏ hàng.',
      },
      {
        heading: 'Bước 2 — Thanh toán',
        body: 'Kiểm tra giỏ hàng, chọn hình thức nhận hàng (giao tận nơi/nhận tại showroom) và phương thức thanh toán phù hợp.',
      },
      {
        heading: 'Bước 3 — Theo dõi đơn hàng',
        body: 'Sau khi đặt hàng thành công, khách hàng có thể theo dõi trạng thái đơn hàng trong mục "Đơn hàng của tôi" hoặc tra cứu bằng mã đơn hàng.',
      },
    ],
  },
};
