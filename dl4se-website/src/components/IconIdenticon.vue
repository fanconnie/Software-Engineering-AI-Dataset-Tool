<script>
import Base64 from "crypto-js/enc-base64";
import md5 from "crypto-js/md5";
import Identicon from "identicon.js";

export default {
  name: "b-icon-identicon",
  functional: true,
  props: {
    identifier: {
      type: String,
      required: true,
    },
    scale: {
      type: Number,
      default: 1,
    },
    // TODO 22.11.22: Introduce support for other BIcon props
    // https://bootstrap-vue.org/docs/icons#component-reference
  },
  render(create